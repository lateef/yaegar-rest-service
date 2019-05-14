package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Location;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findByProductAndLocation(Product product, Location location);

    Optional<Stock> findByProductAndCompanyStockId(Product product, UUID companyStockId);

    List<Stock> findByAccountsIn(List<Account> accounts);

    List<Stock> findByCompanyStockId(UUID companyStockId);
}
