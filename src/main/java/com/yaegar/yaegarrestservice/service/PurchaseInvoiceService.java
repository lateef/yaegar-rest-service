package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.joining;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService {
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public void computeInventory(PurchaseOrder purchaseOrder) {
        purchaseOrder.getInvoices()
                .forEach(purchaseInvoice -> {
                    final List<StockTransaction> stockTransactions = purchaseInvoice.getLineItems()
                            .stream()
                            .map(lineItem -> {
                                final StockTransaction stockTransaction = new StockTransaction();
                                stockTransaction.setPurchaseInvoice(purchaseInvoice);
                                stockTransaction.setProduct(lineItem.getProduct());
                                stockTransaction.setQuantity(lineItem.getQuantity());
                                stockTransaction.setLocation(purchaseOrder.getSupplier().getPrincipalCompany().getLocations().get(0));
                                return stockTransactionRepository.save(stockTransaction);
                            })
                            .collect(Collectors.toList());

                    final List<Stock> stocks = stockTransactions.stream()
                            .map(stockTransaction -> {
                                final Stock stock = stockRepository
                                        .findByProductAndLocation(stockTransaction.getProduct(),
                                                stockTransaction.getLocation())
                                        .orElse(new Stock());

                                if (stock.getId() != null) {
                                    final double quantity = stockTransactionRepository.findByProduct(stockTransaction.getProduct())
                                            .stream()
                                            .mapToDouble(StockTransaction::getQuantity)
                                            .sum();
                                    stock.setQuantity(quantity);
                                } else {
                                    final Product product = stockTransaction.getProduct();
                                    stock.setProduct(product);
                                    stock.setLocation(stockTransaction.getLocation());
                                    stock.setQuantity(stockTransaction.getQuantity());
                                    final Company principalCompany = purchaseOrder.getSupplier().getPrincipalCompany();
                                    stock.setCompanyStockId(principalCompany.getId());
                                    stock.setLocation(principalCompany.getLocations().get(0));

                                    final BigDecimal costPrice = stockTransaction.getPurchaseInvoice().getLineItems().stream()
                                            .filter(lineItem -> lineItem.getProduct().getId().equals(product.getId()))
                                            .map(AbstractLineItem::getUnitPrice)
                                            .findAny()
                                            .orElseThrow(NullPointerException::new);
                                    stock.setCostPrice(costPrice);
                                }
                                return stock;
                            })
                            .collect(Collectors.toList());

                    stockRepository.saveAll(stocks);
                });
    }

    public Set<PurchaseInvoice> processInvoices(Set<PurchaseInvoice> invoices, Set<PurchaseInvoice> savedInvoices) {
        return invoices.stream()
                .map(invoice -> {
                    final PurchaseInvoice savedPurchaseInvoice = savedInvoices.stream()
                            .filter(purchaseInvoice -> purchaseInvoice.getId().equals(invoice.getId()))
                            .findFirst()
                            .orElse(null);
                    if (Objects.nonNull(savedPurchaseInvoice)) {
                        invoice.setCreatedBy(savedPurchaseInvoice.getCreatedBy());
                        invoice.setUpdatedBy(savedPurchaseInvoice.getUpdatedBy());

                        final Set<PurchaseInvoiceLineItem> purchaseInvoiceLineItems = invoice.getLineItems().stream()
                                .map(lineItem -> {
                                    final PurchaseInvoiceLineItem savedPurchaseInvoiceLineItem = savedPurchaseInvoice.getLineItems().stream()
                                            .filter(savedLineItem -> savedLineItem.getId().equals(lineItem.getId()))
                                            .findFirst()
                                            .orElse(null);
                                    if (Objects.nonNull(savedPurchaseInvoiceLineItem)) {
                                        lineItem.setCreatedBy(savedPurchaseInvoiceLineItem.getCreatedBy());
                                        lineItem.setUpdatedBy(savedPurchaseInvoiceLineItem.getUpdatedBy());
                                    }
                                    return lineItem;
                                })
                                .collect(toSet());
                        invoice.setLineItems(purchaseInvoiceLineItems);
                    }
                    return invoice;
                })
                .map(this::validateAndSumSubTotal)
                .collect(toSet());
    }

    public String confirmValidInvoice(PurchaseOrder purchaseOrder, PurchaseOrder savedPurchaseOrder) {
        final PurchaseInvoice newPurchaseInvoice = getNewPurchaseInvoice(purchaseOrder);

        final Map<UUID, Double> lineItemTotalsGroupedByPurchaseOrderLineItemId = savedPurchaseOrder.getInvoices().stream()
                .flatMap(invoice -> invoice.getLineItems().stream())
                .collect(groupingBy(
                        PurchaseInvoiceLineItem::getPurchaseOrderLineItemId,
                        mapping(AbstractLineItem::getQuantity, Collectors.summingDouble(Double::valueOf))));

        List<String> confirmMessages = new ArrayList<>();

        newPurchaseInvoice.getLineItems()
                .forEach(lineItem -> {
                    final UUID purchaseOrderLineItemId = lineItem.getPurchaseOrderLineItemId();
                    final double quantityDelivered = Optional.ofNullable(
                            lineItemTotalsGroupedByPurchaseOrderLineItemId.get(purchaseOrderLineItemId))
                            .orElse(new Double("0"));
                    final @NotNull double purchaseInvoiceQuantity = lineItem.getQuantity();

                    final double totalQuantityOrdered = savedPurchaseOrder.getLineItems().stream()
                            .filter(lineItem1 -> lineItem1.getId().equals(purchaseOrderLineItemId))
                            .map(AbstractLineItem::getQuantity)
                            .findFirst()
                            .orElseThrow(NullPointerException::new);

                    if ((quantityDelivered + purchaseInvoiceQuantity) > totalQuantityOrdered) {
                        confirmMessages.add(lineItem.getProduct().getTitle() + " exceeds request by " + ((quantityDelivered + purchaseInvoiceQuantity) - totalQuantityOrdered));
                    }
                });

        return (confirmMessages.size() > 0) ? confirmMessages.stream()
                .collect(joining(", ", "error:", "")) : "";
    }

    public Set<PurchaseInvoiceLineItem> validateInvoiceLineItems(Set<PurchaseInvoiceLineItem> lineItems) {
        lineItems.stream()
                .forEach(lineItem -> {
                    Product product = productRepository
                            .findById(lineItem
                                    .getProduct()
                                    .getId())
                            .orElseThrow(NullPointerException::new);

                    lineItem.setProduct(product);
                    lineItem.setSubTotal(lineItem.getUnitPrice().multiply(BigDecimal.valueOf(lineItem.getQuantity())));
                });
        return new HashSet<>(lineItems);
    }

    public BigDecimal sumLineInvoiceItemsSubTotal(Set<PurchaseInvoiceLineItem> lineItems) {
        return lineItems.stream()
                .map(PurchaseInvoiceLineItem::getSubTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    private PurchaseInvoice getNewPurchaseInvoice(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getInvoices().stream()
                .filter(invoice -> Objects.isNull(invoice.getCreatedDateTime()))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    private PurchaseInvoice validateAndSumSubTotal(PurchaseInvoice invoice) {
        final Set<PurchaseInvoiceLineItem> lineItems = validateInvoiceLineItems(invoice.getLineItems());
        invoice.setLineItems(lineItems);
        invoice.setTotalPrice(sumLineInvoiceItemsSubTotal(lineItems));
        return invoice;
    }
}
