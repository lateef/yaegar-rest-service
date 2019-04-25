package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
}
