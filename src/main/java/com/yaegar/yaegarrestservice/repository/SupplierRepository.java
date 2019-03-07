package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findById(Long id);
    List<Supplier> findByPrincipalCompanyId(Long principalCompany);
}