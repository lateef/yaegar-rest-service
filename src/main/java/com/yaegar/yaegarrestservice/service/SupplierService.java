package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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

    public Supplier saveSupplier(Supplier supplier, User user) {
        if (Objects.isNull(supplier.getId())) {
            supplier.setName(supplier.getName().trim());
            supplier.setCreatedBy(user.getId());
        }
        supplier.setUpdatedBy(user.getId());
        return supplierRepository.save(supplier);
    }

    public List<Supplier> getSuppliersByPrincipalCompanyId(Long principalCompanyId) {
        return supplierRepository.findByPrincipalCompanyId(principalCompanyId);
    }

    public Supplier getSupplierProductsById(Long supplierId) {
        return supplierRepository.findOneWithProductsById(supplierId);
    }
}
