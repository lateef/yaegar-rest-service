package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findAllBySupplierPrincipalCompanyId(UUID companyId);
}
