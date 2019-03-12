package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Invoice;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.model.StockTransaction;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.InvoiceRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InvoiceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, StockRepository stockRepository, StockTransactionRepository stockTransactionRepository) {
        this.invoiceRepository = invoiceRepository;
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    public List<Invoice> saveAll(Set<Invoice> invoices) {
        return invoiceRepository.saveAll(invoices);
    }

    public void computeInventory(PurchaseOrder purchaseOrder, User updatedBy) {
                purchaseOrder.getInvoices()
                .forEach(invoice -> {
                            final List<StockTransaction> stockTransactions1 = invoice.getLineItems()
                                    .stream()
                                    .map(lineItem -> {
                                        final StockTransaction stockTransaction = new StockTransaction();
                                        stockTransaction.setInvoice(invoice);
                                        stockTransaction.setProduct(lineItem.getProduct());
                                        stockTransaction.setQuantity(lineItem.getQuantity());
                                        stockTransaction.setToLocation(null);
                                        if (Objects.isNull(stockTransaction.getCreatedBy())) {
                                            stockTransaction.setCreatedBy(updatedBy.getId());
                                        }
                                        stockTransaction.setUpdatedBy(updatedBy.getId());
                                        return stockTransaction;
                                    })
                                    .collect(Collectors.toList());

                            final List<StockTransaction> stockTransactions2 = stockTransactionRepository.saveAll(stockTransactions1);

                            final List<Stock> stocks = stockTransactions2.stream()
                                    .map(stockTransaction -> {
                                        final Stock stock = stockRepository
                                                .findByProductAndLocation(stockTransaction.getProduct(),
                                                        stockTransaction.getToLocation())
                                                .orElse(new Stock());

                                        if (stock.getId() != null) {
                                            final double quantity = stockTransactionRepository.findByProduct(stockTransaction.getProduct())
                                                    .stream()
                                                    .mapToDouble(StockTransaction::getQuantity)
                                                    .sum();
                                            stock.setQuantity(quantity);
                                        } else {
                                            stock.setProduct(stockTransaction.getProduct());
                                            stock.setLocation(stockTransaction.getToLocation());
                                            stock.setQuantity(stockTransaction.getQuantity());
                                        }
                                        if (Objects.isNull(stock.getCreatedBy())) {
                                            stock.setCreatedBy(updatedBy.getId());
                                        }
                                        stock.setUpdatedBy(updatedBy.getId());
                                        return stock;
                                    })
                                    .collect(Collectors.toList());

                            stockRepository.saveAll(stocks);
                        });
    }

    public List<Invoice> sortInvoicesByDate(Set<Invoice> invoices) {
        return invoices
                .stream()
                .sorted(Comparator.comparing(Invoice::getCreatedDatetime))
                .collect(Collectors.toList());
    }
}
