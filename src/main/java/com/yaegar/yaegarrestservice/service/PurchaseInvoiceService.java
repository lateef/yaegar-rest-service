package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.PurchaseInvoice;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.model.StockTransaction;
import com.yaegar.yaegarrestservice.repository.PurchaseInvoiceRepository;
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
public class PurchaseInvoiceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseInvoiceService.class);

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public PurchaseInvoiceService(PurchaseInvoiceRepository purchaseInvoiceRepository, StockRepository stockRepository, StockTransactionRepository stockTransactionRepository) {
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    public List<PurchaseInvoice> saveAll(Set<PurchaseInvoice> invoices) {
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
}
