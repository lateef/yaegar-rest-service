package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import com.yaegar.yaegarrestservice.repository.SupplierRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PurchaseOrderServiceTest {
    @MockBean
    private ProductRepository productRepository;
    @MockBean
    private PurchaseOrderRepository purchaseOrderRepository;
    @MockBean
    private StockRepository stockRepository;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;
    @MockBean
    private SupplierRepository supplierRepository;

    private PurchaseOrderService purchaseOrderService;

    @Before
    public void setup() {
        purchaseOrderService = new PurchaseOrderService(
                productRepository,
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