package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Invoice;
import com.yaegar.yaegarrestservice.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class InvoiceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceService.class);

    private InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> saveAll(Set<Invoice> invoices) {
        return invoiceRepository.saveAll(invoices);
    }
}
