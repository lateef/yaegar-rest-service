package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByPrincipalCompanyId(Long principalCompany);
    Supplier findOneWithProductsById(Long supplierId);
}