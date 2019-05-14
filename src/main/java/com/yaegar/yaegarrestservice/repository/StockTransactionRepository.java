package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
    List<StockTransaction> findByProduct(Product product);
}
