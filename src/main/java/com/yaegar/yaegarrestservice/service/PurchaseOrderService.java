package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.PurchaseOrderActivity;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public Optional<PurchaseOrder> getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public List<PurchaseOrder> getPurchaseOrders(Long companyId) {
        return purchaseOrderRepository.findAllByCompanyId(companyId);
    }

    public PurchaseOrder addPurchaseOrderActivity(PurchaseOrder purchaseOrder,
                                                  PurchaseOrderActivity purchaseOrderActivity,
                                                  User updatedBy) {
        purchaseOrder.setPurchaseOrderState(purchaseOrderActivity.getPurchaseOrderState());
        purchaseOrder.getPurchaseOrderActivities().add(purchaseOrderActivity);
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public PurchaseOrder addPurchaseOrderSupplyActivity(PurchaseOrder purchaseOrder,
                                                        PurchaseOrderActivity purchaseOrderActivity,
                                                        User updatedBy) {
        purchaseOrder.setOrderSupplyState(purchaseOrderActivity.getOrderSupplyState());
        purchaseOrder.getPurchaseOrderActivities().add(purchaseOrderActivity);
        return purchaseOrderRepository.save(purchaseOrder);
    }
}
