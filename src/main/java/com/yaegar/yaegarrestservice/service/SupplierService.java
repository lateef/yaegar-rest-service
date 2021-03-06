package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public Optional<Supplier> findById(UUID id) {
        return supplierRepository.findById(id);
    }

    public Supplier saveSupplier(Supplier supplier) {
        if (Objects.isNull(supplier.getId())) {
            supplier.setName(supplier.getName().trim());
        }
        return supplierRepository.save(supplier);
    }

    public List<Supplier> getSuppliersByPrincipalCompanyId(UUID principalCompanyId) {
        return supplierRepository.findByPrincipalCompanyId(principalCompanyId);
    }

    public Supplier getSupplierProductsById(UUID supplierId) {
        return supplierRepository.findOneWithProductsById(supplierId);
    }
}
