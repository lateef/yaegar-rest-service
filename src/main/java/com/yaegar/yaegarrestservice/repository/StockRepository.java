package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Location;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductAndLocation(Product product, Location location);
}
