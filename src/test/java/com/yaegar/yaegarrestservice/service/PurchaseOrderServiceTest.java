package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Payment;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.PaymentRepository;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class PurchaseOrderServiceTest {
    @MockBean
    private PaymentRepository paymentRepository;
    @MockBean
    private PurchaseOrderRepository purchaseOrderRepository;
    @MockBean
    private StockRepository stockRepository;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;

    private PurchaseOrderService purchaseOrderService;

    @Before
    public void setup() {
        purchaseOrderService = new PurchaseOrderService(paymentRepository, purchaseOrderRepository, stockRepository, stockTransactionRepository);
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
    public void savePayments() {
        //given
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        PurchaseOrder expectedPurchaseOrder = new PurchaseOrder();
        Set<Payment> payments = new HashSet<>();
        User createdBy = new User();

        when(purchaseOrderRepository.save(purchaseOrder)).thenReturn(expectedPurchaseOrder);

        //when
        final PurchaseOrder actualPurchaseOrder = purchaseOrderService.savePayments(purchaseOrder, payments, createdBy);

        //then
        assertThat(actualPurchaseOrder, is(sameBeanAs(expectedPurchaseOrder)));
    }

    @Test
    public void addPurchaseOrderSupplyActivity() {
    }
}