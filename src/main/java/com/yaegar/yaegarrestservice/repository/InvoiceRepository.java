package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
