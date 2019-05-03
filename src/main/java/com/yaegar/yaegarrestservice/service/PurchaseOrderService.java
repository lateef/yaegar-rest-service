package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.PurchaseInvoiceLineItem;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.PurchaseOrderLineItem;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;

@Service
public class PurchaseOrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderService.class);

    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrderService(ProductRepository productRepository, PurchaseOrderRepository purchaseOrderRepository) {
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public PurchaseOrder savePurchaseOrder(PurchaseOrder purchaseOrder) {
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public Optional<PurchaseOrder> getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public List<PurchaseOrder> getPurchaseOrders(Long companyId) {
        return purchaseOrderRepository.findAllBySupplierPrincipalCompanyId(companyId);
    }

    public Set<PurchaseOrderLineItem> validateOrderLineItems(List<PurchaseOrderLineItem> lineItems) {
        IntStream.range(0, lineItems.size())
                .forEach(idx -> {
                    final PurchaseOrderLineItem lineItem = lineItems.get(idx);
                    lineItem.setEntry(idx + 1);

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

    public List<PurchaseOrderLineItem> sortOrderLineItemsIntoOrderedList(Set<PurchaseOrderLineItem> lineItems) {
        return lineItems.stream()
                .sorted(Comparator.comparing(PurchaseOrderLineItem::getEntry))
                .collect(Collectors.toList());
    }
}
