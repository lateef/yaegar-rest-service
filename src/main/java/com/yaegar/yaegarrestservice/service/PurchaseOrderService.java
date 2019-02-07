package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseOrderService {

    private PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public PurchaseOrder addPurchaseOrder(PurchaseOrder purchaseOrder, User createdBy) {
        purchaseOrder.getLineItems().forEach(lineItem -> {
            lineItem.setCreatedBy(createdBy.getId());
            lineItem.setUpdatedBy(createdBy.getId());
        });
        purchaseOrder.setCreatedBy(createdBy.getId());
        purchaseOrder.setUpdatedBy(createdBy.getId());
        PurchaseOrder purchaseOrder1 = purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrder1;
    }

    public List<PurchaseOrder> getPurchaseOrders(Long companyId) {
        return purchaseOrderRepository.findAllByCompanyId(companyId);
    }
}
