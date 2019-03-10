package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PurchaseOrderServiceTest {
    @MockBean
    private PurchaseOrderRepository purchaseOrderRepository;
    @MockBean
    private StockRepository stockRepository;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;

    private PurchaseOrderService purchaseOrderService;

    @Before
    public void setup() {
        purchaseOrderService = new PurchaseOrderService(
                purchaseOrderRepository,
                stockRepository,
                stockTransactionRepository
        );
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
    public void addPurchaseOrderSupplyActivity() {
    }
}