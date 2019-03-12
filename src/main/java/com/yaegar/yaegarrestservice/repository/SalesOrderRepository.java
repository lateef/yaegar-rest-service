package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findAllByCustomerPrincipalCompanyId(Long companyId);
}
