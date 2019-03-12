package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findAllBySupplierPrincipalCompanyId(Long companyId);
}
