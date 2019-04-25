package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
}
