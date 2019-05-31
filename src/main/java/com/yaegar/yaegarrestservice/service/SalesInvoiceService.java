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

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesInvoiceService {
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public void computeInventory(SalesOrder salesOrder) {
        salesOrder.getInvoices()
                .forEach(salesInvoice -> {
                    final List<StockTransaction> stockTransactions = salesInvoice.getLineItems()
                            .stream()
                            .map(lineItem -> {
                                final Optional<StockTransaction> optionalStockTransaction = stockTransactionRepository.findBySalesInvoiceLineItem(lineItem);

                                if (!optionalStockTransaction.isPresent()) {
                                    final StockTransaction stockTransaction = new StockTransaction();
                                    stockTransaction.setSalesInvoiceLineItem(lineItem);
                                    stockTransaction.setLocation(salesOrder.getCustomer().getPrincipalCompany().getLocations().get(0));
                                    return stockTransactionRepository.save(stockTransaction);
                                } else {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(toList());

                    final List<Stock> stocks = stockTransactions.stream()
                            .map(stockTransaction -> {
                                final Stock stock = stockRepository
                                        .findByProductAndLocation(stockTransaction.getSalesInvoiceLineItem().getProduct(),
                                                stockTransaction.getLocation())
                                        .orElseThrow(NullPointerException::new);

                                final double purchaseQuantity = stockTransactionRepository.findByPurchaseInvoiceLineItemProduct(stockTransaction.getPurchaseInvoiceLineItem().getProduct())
                                        .stream()
                                        .mapToDouble(stockTransaction1 -> stockTransaction1.getPurchaseInvoiceLineItem().getQuantity())
                                        .sum();

                                final double salesQuantity = stockTransactionRepository.findBySalesInvoiceLineItemProduct(stockTransaction.getSalesInvoiceLineItem().getProduct())
                                        .stream()
                                        .mapToDouble(stockTransaction1 -> stockTransaction1.getSalesInvoiceLineItem().getQuantity())
                                        .sum();
                                stock.setQuantity(purchaseQuantity - salesQuantity);
                                return stock;
                            })
                            .collect(toList());

                    stockRepository.saveAll(stocks);
                });
    }

    public Set<SalesInvoice> processInvoices(Set<SalesInvoice> invoices, Set<SalesInvoice> savedInvoices) {
        return invoices.stream()
                .map(invoice -> {
                    final SalesInvoice savedSalesInvoice = savedInvoices.stream()
                            .filter(salesInvoice -> salesInvoice.getId().equals(invoice.getId()))
                            .findFirst()
                            .orElse(null);
                    if (Objects.nonNull(savedSalesInvoice)) {
                        invoice.setCreatedBy(savedSalesInvoice.getCreatedBy());
                        invoice.setUpdatedBy(savedSalesInvoice.getUpdatedBy());

                        final Set<SalesInvoiceLineItem> salesInvoiceLineItems = invoice.getLineItems().stream()
                                .map(lineItem -> {
                                    final SalesInvoiceLineItem savedSalesInvoiceLineItem = savedSalesInvoice.getLineItems().stream()
                                            .filter(savedLineItem -> savedLineItem.getId().equals(lineItem.getId()))
                                            .findFirst()
                                            .orElse(null);
                                    if (Objects.nonNull(savedSalesInvoiceLineItem)) {
                                        lineItem.setCreatedBy(savedSalesInvoiceLineItem.getCreatedBy());
                                        lineItem.setUpdatedBy(savedSalesInvoiceLineItem.getUpdatedBy());
                                    }
                                    return lineItem;
                                })
                                .collect(toSet());
                        invoice.setLineItems(salesInvoiceLineItems);
                    }
                    return invoice;
                })
                .map(this::validateAndSumSubTotal)
                .collect(toSet());
    }

    public String confirmValidInvoice(SalesOrder salesOrder, SalesOrder savedSalesOrder) {
        final SalesInvoice newSalesInvoice = getNewSalesInvoice(salesOrder);

        final Map<UUID, Double> lineItemTotalsGroupedBySalesOrderLineItemId = savedSalesOrder.getInvoices().stream()
                .flatMap(invoice -> invoice.getLineItems().stream())
                .collect(groupingBy(
                        SalesInvoiceLineItem::getSalesOrderLineItemId,
                        mapping(AbstractLineItem::getQuantity, Collectors.summingDouble(Double::valueOf))));

        List<String> confirmMessages = new ArrayList<>();

        newSalesInvoice.getLineItems()
                .forEach(lineItem -> {
                    final UUID salesOrderLineItemId = lineItem.getSalesOrderLineItemId();
                    final double quantityDelivered = Optional.ofNullable(
                            lineItemTotalsGroupedBySalesOrderLineItemId.get(salesOrderLineItemId))
                            .orElse(new Double("0"));
                    final @NotNull double salesInvoiceQuantity = lineItem.getQuantity();

                    final double totalQuantityOrdered = savedSalesOrder.getLineItems().stream()
                            .filter(lineItem1 -> lineItem1.getId().equals(salesOrderLineItemId))
                            .map(AbstractLineItem::getQuantity)
                            .findFirst()
                            .orElseThrow(NullPointerException::new);

                    if ((quantityDelivered + salesInvoiceQuantity) > totalQuantityOrdered) {
                        confirmMessages.add(lineItem.getProduct().getTitle() + " exceeds request by " + ((quantityDelivered + salesInvoiceQuantity) - totalQuantityOrdered));
                    }
                });

        return (confirmMessages.size() > 0) ? confirmMessages.stream()
                .collect(joining(", ", "error:", "")) : "";
    }

    public Set<SalesInvoiceLineItem> validateInvoiceLineItems(Set<SalesInvoiceLineItem> lineItems) {
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

    public BigDecimal sumLineInvoiceItemsSubTotal(Set<SalesInvoiceLineItem> lineItems) {
        return lineItems.stream()
                .map(SalesInvoiceLineItem::getSubTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    public String confirmStockAvailability(SalesOrder salesOrder, SalesOrder savedSalesOrder) {
        final SalesInvoice newSalesInvoice = getNewSalesInvoice(salesOrder);

        List<String> availabilityMessages = new ArrayList<>();

        newSalesInvoice.getLineItems()
                .forEach(lineItem -> {
                    final Location location = savedSalesOrder.getCustomer().getPrincipalCompany().getLocations().stream()
                            .findAny().orElseThrow(NullPointerException::new);
                    final Double quantityInStock = stockRepository.findByProductAndLocation(lineItem.getProduct(), location)
                            .orElseThrow(NullPointerException::new).getQuantity();
                    final @NotNull double salesInvoiceQuantity = lineItem.getQuantity();

                    if (salesInvoiceQuantity > quantityInStock) {
                        availabilityMessages.add(lineItem.getProduct().getTitle() + " insufficient stock quantity");
                    }
                });

        return (availabilityMessages.size() > 0) ? availabilityMessages.stream()
                .collect(joining(", ", "error:", "")) : "";
    }

    private SalesInvoice getNewSalesInvoice(SalesOrder salesOrder) {
        return salesOrder.getInvoices().stream()
                .filter(invoice -> Objects.isNull(invoice.getCreatedDateTime()))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    private SalesInvoice validateAndSumSubTotal(SalesInvoice invoice) {
        final Set<SalesInvoiceLineItem> lineItems = validateInvoiceLineItems(invoice.getLineItems());
        invoice.setLineItems(lineItems);
        invoice.setTotalPrice(sumLineInvoiceItemsSubTotal(lineItems));
        return invoice;
    }
}
