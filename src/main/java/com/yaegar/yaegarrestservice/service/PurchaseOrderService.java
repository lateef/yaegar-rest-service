package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PurchaseOrderService extends OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderService.class);

    private final PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrderService(
            ProductRepository productRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        super(productRepository);
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public PurchaseOrder savePurchaseOrder(PurchaseOrder purchaseOrder, User createdBy) {
        if (Objects.isNull(purchaseOrder.getCreatedBy())) {
            purchaseOrder.setCreatedBy(createdBy.getId());
        }
        purchaseOrder.setUpdatedBy(createdBy.getId());
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public Optional<PurchaseOrder> getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public List<PurchaseOrder> getPurchaseOrders(Long companyId) {
        return purchaseOrderRepository.findAllBySupplierPrincipalCompanyId(companyId);
    }
}
