package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.PurchaseInvoiceLineItem;
import com.yaegar.yaegarrestservice.model.SalesInvoiceLineItem;
import com.yaegar.yaegarrestservice.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
    List<StockTransaction> findByPurchaseInvoiceLineItemProduct(Product product);

    List<StockTransaction> findBySalesInvoiceLineItemProduct(Product product);

    Optional<StockTransaction> findByPurchaseInvoiceLineItem(PurchaseInvoiceLineItem purchaseInvoiceLineItem);

    Optional<StockTransaction> findBySalesInvoiceLineItem(SalesInvoiceLineItem salesInvoiceLineItem);
}
