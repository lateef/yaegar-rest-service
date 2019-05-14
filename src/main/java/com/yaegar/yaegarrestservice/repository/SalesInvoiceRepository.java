package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, UUID> {
}
