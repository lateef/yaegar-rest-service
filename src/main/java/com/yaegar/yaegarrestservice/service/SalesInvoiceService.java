package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesInvoiceService {
    private final DateTimeProvider dateTimeProvider;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public void computeInventory(Set<SalesInvoice> invoices) {
        invoices
                .forEach(salesInvoice -> {
                    final List<StockTransaction> stockTransactions1 = salesInvoice.getLineItems()
                            .stream()
                            .map(lineItem -> {
                                final StockTransaction stockTransaction = new StockTransaction();
                                stockTransaction.setSalesInvoice(salesInvoice);
                                stockTransaction.setProduct(lineItem.getProduct());
                                stockTransaction.setQuantity(-1 * lineItem.getQuantity());
                                stockTransaction.setLocation(null);
                                return stockTransaction;
                            })
                            .collect(Collectors.toList());

                    final List<StockTransaction> stockTransactions2 = stockTransactionRepository.saveAll(stockTransactions1);

                    final List<Stock> stocks = stockTransactions2.stream()
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
                                    stock.setProduct(stockTransaction.getProduct());
                                    stock.setLocation(stockTransaction.getLocation());
                                    stock.setQuantity(stockTransaction.getQuantity());
                                }
                                return stock;
                            })
                            .collect(Collectors.toList());

                    stockRepository.saveAll(stocks);
                });
    }

    public List<SalesInvoice> processInvoices(Set<SalesInvoice> invoices) {
        return invoices.stream()
                .map(invoice -> {
                    if (Objects.isNull(invoice.getCreatedDateTime())) {
                        invoice.setCreatedDateTime(dateTimeProvider.now());
                    }
                    return invoice;
                })
                .collect(toCollection(() -> new TreeSet<>(comparing(SalesInvoice::getCreatedDateTime))))
                .stream()
                .map(this::sortValidateAndSumSubTotal)
                .collect(Collectors.toList());
    }

    private SalesInvoice sortValidateAndSumSubTotal(SalesInvoice invoice) {
        final Set<SalesInvoiceLineItem> lineItems = validateInvoiceLineItems(invoice.getLineItems());
        invoice.setLineItems(lineItems);
        invoice.setTotalPrice(sumLineInvoiceItemsSubTotal(lineItems));
        return invoice;
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
}
