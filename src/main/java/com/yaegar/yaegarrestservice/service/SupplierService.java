package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SupplierService.class);

    private SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id);
    }

    public Supplier addSupplier(Supplier supplier) {
        supplier.setName(supplier.getName().trim());
        return supplierRepository.save(supplier);
    }

    public List<Supplier> getSuppliersByCompanyId(Long companyId) {
        return supplierRepository.findByCompanyId(companyId);
    }
}
