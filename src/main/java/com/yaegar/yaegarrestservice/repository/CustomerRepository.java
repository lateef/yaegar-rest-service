package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findByPrincipalCompanyId(UUID companyId);
}