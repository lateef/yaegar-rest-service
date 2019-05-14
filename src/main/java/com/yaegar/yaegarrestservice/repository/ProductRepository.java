package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCompanyId(UUID companyId);
}