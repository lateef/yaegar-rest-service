package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.PurchaseOrderEvent;
import com.yaegar.yaegarrestservice.model.PurchaseOrderLineItem;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderEventType;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;

@Slf4j
@RequiredArgsConstructor
@Service
public class PurchaseOrderService {
    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrder savePurchaseOrder(PurchaseOrder purchaseOrder) {
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public Optional<PurchaseOrder> getPurchaseOrder(UUID id) {
        return purchaseOrderRepository.findById(id);
    }

    public List<PurchaseOrder> getPurchaseOrders(UUID companyId) {
        return purchaseOrderRepository.findAllBySupplierPrincipalCompanyId(companyId);
    }

    public Set<PurchaseOrderLineItem> validateOrderLineItems(Set<PurchaseOrderLineItem> lineItems) {
        lineItems.stream()
                .forEach(lineItem -> {
                    Product product = productRepository
                            .findById(lineItem
                                    .getProduct()
                                    .getId())
                            .orElseThrow(NullPointerException::new);

                    lineItem.setProduct(product);
                    lineItem.setSubTotal(lineItem.getUnitPrice().multiply(BigDecimal.valueOf(lineItem.getQuantity())));
                });
        return new HashSet<>(lineItems);
    }

    public BigDecimal sumLineOrderItemsSubTotal(Set<PurchaseOrderLineItem> lineItems) {
        return lineItems.stream()
                .map(PurchaseOrderLineItem::getSubTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    public PurchaseOrder addEvent(PurchaseOrder purchaseOrder, String description, PurchaseOrderEventType orderEventType) {
        final Set<PurchaseOrderEvent> purchaseOrderEvents = purchaseOrder.getPurchaseOrderEvents();
        final PurchaseOrderEvent purchaseOrderEvent = new PurchaseOrderEvent(orderEventType, description);
        purchaseOrderEvents.add(purchaseOrderEvent);
        purchaseOrder.setPurchaseOrderEvents(purchaseOrderEvents);
        return purchaseOrder;
    }
}
