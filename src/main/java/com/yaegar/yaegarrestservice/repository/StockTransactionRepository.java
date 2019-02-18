package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByProduct(Product product);
}
