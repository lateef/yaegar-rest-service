package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.PurchaseInvoice;
import com.yaegar.yaegarrestservice.model.PurchaseInvoiceLineItem;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.model.StockTransaction;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.PurchaseInvoiceRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

@Service
public class PurchaseInvoiceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseInvoiceService.class);

    private final DateTimeProvider dateTimeProvider;
    private final ProductRepository productRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public PurchaseInvoiceService(DateTimeProvider dateTimeProvider,
                                  ProductRepository productRepository,
                                  PurchaseInvoiceRepository purchaseInvoiceRepository,
                                  StockRepository stockRepository,
                                  StockTransactionRepository stockTransactionRepository) {
        this.dateTimeProvider = dateTimeProvider;
        this.productRepository = productRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    public List<PurchaseInvoice> saveAll(List<PurchaseInvoice> invoices) {
        return purchaseInvoiceRepository.saveAll(invoices);
    }

    public void computeInventory(Set<PurchaseInvoice> purchaseInvoices) {
        purchaseInvoices
                .forEach(purchaseInvoice -> {
                    final List<StockTransaction> stockTransactions1 = purchaseInvoice.getLineItems()
                            .stream()
                            .map(lineItem -> {
                                final StockTransaction stockTransaction = new StockTransaction();
                                stockTransaction.setPurchaseInvoice(purchaseInvoice);
                                stockTransaction.setProduct(lineItem.getProduct());
                                stockTransaction.setQuantity(lineItem.getQuantity());
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

    public List<PurchaseInvoice> sortInvoicesByDate(Set<PurchaseInvoice> purchaseInvoices) {
        return purchaseInvoices
                .stream()
                .sorted(Comparator.comparing(PurchaseInvoice::getCreatedDatetime))
                .collect(Collectors.toList());
    }

    public Set<PurchaseInvoiceLineItem> validateInvoiceLineItems(List<PurchaseInvoiceLineItem> lineItems) {
        IntStream.range(0, lineItems.size())
                .forEach(idx -> {
                    final PurchaseInvoiceLineItem lineItem = lineItems.get(idx);
                    lineItem.setEntry(idx + 1);

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

    public List<PurchaseInvoiceLineItem> sortInvoiceLineItemsIntoOrderedList(Set<PurchaseInvoiceLineItem> lineItems) {
        return lineItems.stream()
                .sorted(Comparator.comparing(PurchaseInvoiceLineItem::getEntry))
                .collect(Collectors.toList());
    }

    public List<PurchaseInvoice> processInvoices(Set<PurchaseInvoice> invoices) {
        return invoices.stream()
                .map(invoice -> {
                    if (Objects.isNull(invoice.getCreatedDatetime())) {
                        invoice.setCreatedDatetime(dateTimeProvider.now());
                    }
                    return invoice;
                })
                .collect(toCollection(() -> new TreeSet<>(comparing(PurchaseInvoice::getCreatedDatetime))))
                .stream()
                .map(this::sortValidateAndSumSubTotal)
                .collect(Collectors.toList());
    }

    private PurchaseInvoice sortValidateAndSumSubTotal(PurchaseInvoice invoice) {
        final List<PurchaseInvoiceLineItem> lineItems = sortInvoiceLineItemsIntoOrderedList(invoice.getLineItems());
        final Set<PurchaseInvoiceLineItem> lineItems1 = validateInvoiceLineItems(lineItems);
        invoice.setLineItems(lineItems1);
        invoice.setTotalPrice(sumLineInvoiceItemsSubTotal(lineItems1));
        return invoice;
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
