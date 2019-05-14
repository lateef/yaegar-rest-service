package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    List<Supplier> findByPrincipalCompanyId(UUID principalCompany);
    Supplier findOneWithProductsById(UUID supplierId);
}