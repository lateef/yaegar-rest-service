package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {
    List<SalesOrder> findAllByCustomerPrincipalCompanyId(UUID companyId);
}
