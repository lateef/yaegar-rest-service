package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
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
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

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
                .map(this::validateAndSumSubTotal)
                .collect(Collectors.toList());
    }

    private SalesInvoice validateAndSumSubTotal(SalesInvoice invoice) {
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
}
