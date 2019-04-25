package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.SalesInvoice;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.model.StockTransaction;
import com.yaegar.yaegarrestservice.repository.SalesInvoiceRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SalesInvoiceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesInvoiceService.class);

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public SalesInvoiceService(SalesInvoiceRepository salesInvoiceRepository, StockRepository stockRepository, StockTransactionRepository stockTransactionRepository) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    public List<SalesInvoice> saveAll(Set<SalesInvoice> salesInvoices) {
        return salesInvoiceRepository.saveAll(salesInvoices);
    }

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

    public List<SalesInvoice> sortInvoicesByDate(Set<SalesInvoice> salesInvoices) {
        return salesInvoices
                .stream()
                .sorted(Comparator.comparing(SalesInvoice::getCreatedDatetime))
                .collect(Collectors.toList());
    }
}
