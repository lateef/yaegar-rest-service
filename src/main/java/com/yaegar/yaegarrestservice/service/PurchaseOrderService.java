package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Invoice;
import com.yaegar.yaegarrestservice.model.JournalEntry;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.PurchaseOrderEvent;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.model.StockTransaction;
import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import com.yaegar.yaegarrestservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.PRODUCT;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.EXPENSES;
import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.GOODS_RECEIVED;
import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.PAID;
import static com.yaegar.yaegarrestservice.model.enums.TransactionSide.DEBIT;

@Service
public class PurchaseOrderService {
    private PurchaseOrderRepository purchaseOrderRepository;
    private StockRepository stockRepository;
    private StockTransactionRepository stockTransactionRepository;
    private TransactionRepository transactionRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                StockRepository stockRepository,
                                StockTransactionRepository stockTransactionRepository,
                                TransactionRepository transactionRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.transactionRepository = transactionRepository;
    }

    public PurchaseOrder addPurchaseOrder(PurchaseOrder purchaseOrder, User createdBy) {
        purchaseOrder.getLineItems().forEach(lineItem -> {
            lineItem.setCreatedBy(createdBy.getId());
            lineItem.setUpdatedBy(createdBy.getId());
        });
        purchaseOrder.setCreatedBy(createdBy.getId());
        purchaseOrder.setUpdatedBy(createdBy.getId());
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public Optional<PurchaseOrder> getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public List<PurchaseOrder> getPurchaseOrders(Long companyId) {
        return purchaseOrderRepository.findAllByCompanyId(companyId);
    }

    public PurchaseOrder saveTransaction(PurchaseOrder purchaseOrder, Transaction transaction, User updatedBy) {
        transaction.setTransactionTypeId(purchaseOrder.getId());

        if (transaction.getId() != null) {
            final Transaction transaction1 = transactionRepository.findById(transaction.getId())
                    .orElseThrow(NullPointerException::new);
            transaction.setCreatedDatetime(transaction1.getCreatedDatetime());
            transaction.setUpdatedBy(updatedBy.getId());
        } else {
            transaction.setCreatedBy(updatedBy.getId());
            transaction.setUpdatedBy(updatedBy.getId());
        }
        final Transaction transaction1 = transactionRepository.save(transaction);

        final int[] i = {2};
        purchaseOrder.getLineItems()
                .forEach(lineItem -> {
                    final Account account1 = lineItem.getProduct().getAccounts()
                            .stream()
                            .filter(account -> account.getAccountType().equals(EXPENSES) && account.getAccountCategory().equals(PRODUCT))
                            .findFirst()
                            .orElseThrow(NullPointerException::new);
                    JournalEntry journalEntry = new JournalEntry();
                    journalEntry.setAccount(account1);
                    journalEntry.setAmount(lineItem.getUnitPrice().multiply(BigDecimal.valueOf(lineItem.getQuantity())));
                    journalEntry.setEntry(i[0]);
                    journalEntry.setTransactionDatetime(LocalDateTime.now());
                    journalEntry.setTransactionId(transaction1.getId());
                    journalEntry.setTransactionSide(DEBIT);
                    i[0]++;
                });

        purchaseOrder.setTransaction(transaction1);
        purchaseOrder.setPurchaseOrderState(PAID);
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public PurchaseOrder saveInvoices(PurchaseOrder purchaseOrder, Set<Invoice> invoices, User updatedBy) {
        //TODO confirm payment was paid before setting PAYMENT and update user on only updated payments
        purchaseOrder.setInvoices(invoices);
        purchaseOrder.setPurchaseOrderState(GOODS_RECEIVED);
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder addPurchaseOrderSupplyActivity(PurchaseOrder purchaseOrder,
                                                        PurchaseOrderEvent purchaseOrderEvent,
                                                        User updatedBy) {
        final PurchaseOrderState purchaseOrderState = purchaseOrderEvent.getPurchaseOrderState();
        switch (purchaseOrderState) {
            case GOODS_RECEIVED:
                final List<StockTransaction> stockTransactions = purchaseOrder.getLineItems()
                        .stream()
                        .map(lineItem -> {
                            final StockTransaction stockTransaction = new StockTransaction();
                            stockTransaction.setPurchaseOrder(purchaseOrder);
                            stockTransaction.setProduct(lineItem.getProduct());
                            stockTransaction.setQuantity(lineItem.getQuantity());
                            stockTransaction.setToLocation(null);
                            return stockTransaction;
                        })
                        .collect(Collectors.toList());

                final List<StockTransaction> stockTransactions1 = stockTransactionRepository.saveAll(stockTransactions);

                final List<Stock> stocks = stockTransactions1.stream()
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
                            return stock;
                        })
                        .collect(Collectors.toList());

                stockRepository.saveAll(stocks);

                break;
            default:
                break;
        }
        purchaseOrder.setPurchaseOrderState(purchaseOrderState);
//        purchaseOrder.getPurchaseOrderActivities().add(purchaseOrderEvent);
        return purchaseOrderRepository.save(purchaseOrder);
    }
}
