package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import com.yaegar.yaegarrestservice.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class PurchaseOrderServiceTest {
    @MockBean
    private PurchaseOrderRepository purchaseOrderRepository;
    @MockBean
    private StockRepository stockRepository;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;
    @MockBean
    private TransactionRepository transactionRepository;

    private PurchaseOrderService purchaseOrderService;

    @Before
    public void setup() {
        purchaseOrderService = new PurchaseOrderService(purchaseOrderRepository, stockRepository, stockTransactionRepository, transactionRepository);
    }

    @Test
    public void addPurchaseOrder() {
    }

    @Test
    public void getPurchaseOrder() {
    }

    @Test
    public void getPurchaseOrders() {
    }

    @Test
    public void saveTransactions() {
        //given
        Set<Transaction> transactions = new HashSet<>();
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setTransactions(transactions);
        purchaseOrder.setLineItems(new HashSet<>());
        PurchaseOrder expectedPurchaseOrder = new PurchaseOrder();
        expectedPurchaseOrder.setTransactions(transactions);
        expectedPurchaseOrder.setLineItems(new HashSet<>());
        User createdBy = new User();

        when(purchaseOrderRepository.save(purchaseOrder)).thenReturn(expectedPurchaseOrder);

        //when
        final PurchaseOrder actualPurchaseOrder = purchaseOrderService.saveTransactions(purchaseOrder, transactions, createdBy);

        //then
        assertThat(actualPurchaseOrder, is(sameBeanAs(expectedPurchaseOrder)));
    }

    @Test
    public void addPurchaseOrderSupplyActivity() {
    }
}