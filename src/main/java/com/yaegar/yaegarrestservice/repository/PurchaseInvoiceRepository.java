package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, UUID> {
}
