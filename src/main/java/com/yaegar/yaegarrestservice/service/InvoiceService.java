package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Invoice;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.InvoiceRepository;
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

    public PurchaseOrder computeInventory(User updatedBy) {
//        final PurchaseOrderState purchaseOrderState = purchaseOrderEvent.getPurchaseOrderState();
//        switch (purchaseOrderState) {
//            case GOODS_RECEIVED:
//                final List<StockTransaction> stockTransactions = purchaseOrder.getLineItems()
//                        .stream()
//                        .map(lineItem -> {
//                            final StockTransaction stockTransaction = new StockTransaction();
//                            stockTransaction.setPurchaseOrder(purchaseOrder);
//                            stockTransaction.setProduct(lineItem.getProduct());
//                            stockTransaction.setQuantity(lineItem.getQuantity());
//                            stockTransaction.setToLocation(null);
//                            return stockTransaction;
//                        })
//                        .collect(Collectors.toList());
//
//                final List<StockTransaction> stockTransactions1 = stockTransactionRepository.saveAll(stockTransactions);
//
//                final List<Stock> stocks = stockTransactions1.stream()
//                        .map(stockTransaction -> {
//                            final Stock stock = stockRepository
//                                    .findByProductAndLocation(stockTransaction.getProduct(),
//                                            stockTransaction.getToLocation())
//                                    .orElse(new Stock());
//
//                            if (stock.getId() != null) {
//                                final double quantity = stockTransactionRepository.findByProduct(stockTransaction.getProduct())
//                                        .stream()
//                                        .mapToDouble(StockTransaction::getQuantity)
//                                        .sum();
//                                stock.setQuantity(quantity);
//                            } else {
//                                stock.setProduct(stockTransaction.getProduct());
//                                stock.setLocation(stockTransaction.getToLocation());
//                                stock.setQuantity(stockTransaction.getQuantity());
//                            }
//                            return stock;
//                        })
//                        .collect(Collectors.toList());
//
//                stockRepository.saveAll(stocks);
//
//                break;
//            default:
//                break;
//        }
//        purchaseOrder.setPurchaseOrderState(purchaseOrderState);
//        purchaseOrder.getPurchaseOrderActivities().add(purchaseOrderEvent);
        return null;
    }

    public List<Invoice> sortInvoicesByDate(Set<Invoice> invoices) {
        return invoices
                .stream()
                .sorted(Comparator.comparing(Invoice::getCreatedDatetime))
                .collect(Collectors.toList());
    }
}
