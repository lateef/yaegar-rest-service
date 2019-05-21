package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.PurchaseInvoiceRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService {
    private final DateTimeProvider dateTimeProvider;
    private final ProductRepository productRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public List<PurchaseInvoice> saveAll(List<PurchaseInvoice> invoices) {
        return purchaseInvoiceRepository.saveAll(invoices);
    }

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

    public List<PurchaseInvoice> sortInvoicesByDate(Set<PurchaseInvoice> purchaseInvoices) {
        return purchaseInvoices
                .stream()
                .sorted(Comparator.comparing(PurchaseInvoice::getCreatedDateTime))
                .collect(Collectors.toList());
    }

    public List<PurchaseInvoice> processInvoices(Set<PurchaseInvoice> invoices) {
        return invoices.stream()
                .map(invoice -> {
                    if (Objects.isNull(invoice.getCreatedDateTime())) {
                        invoice.setCreatedDateTime(dateTimeProvider.now());
                        invoice.setNumber(UUID.randomUUID());
                    }
                    return invoice;
                })
                .collect(toCollection(() -> new TreeSet<>(comparing(PurchaseInvoice::getCreatedDateTime))))
                .stream()
                .map(this::validateAndSumSubTotal)
                .collect(Collectors.toList());
    }

    private PurchaseInvoice validateAndSumSubTotal(PurchaseInvoice invoice) {
        final Set<PurchaseInvoiceLineItem> lineItems = validateInvoiceLineItems(invoice.getLineItems());
        invoice.setLineItems(lineItems);
        invoice.setTotalPrice(sumLineInvoiceItemsSubTotal(lineItems));
        return invoice;
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

    public List<PurchaseInvoice> filterSavedInvoices(Set<PurchaseInvoice> invoices) {
        return invoices.stream()
                .filter(invoice -> invoice.getId() != null)
                .collect(Collectors.toList());
    }

    public List<PurchaseInvoice> filterUnsavedInvoices(Set<PurchaseInvoice> invoices) {
        return invoices.stream()
                .filter(invoice -> invoice.getId() == null)
                .collect(Collectors.toList());
    }

    public BigDecimal sumTotal(List<PurchaseInvoice> purchaseInvoices) {
        return purchaseInvoices.stream()
                .flatMap(purchaseInvoice -> purchaseInvoice.getLineItems().stream())
                .map(PurchaseInvoiceLineItem::getSubTotal)
                .reduce(ZERO, BigDecimal::add);
    }
}
