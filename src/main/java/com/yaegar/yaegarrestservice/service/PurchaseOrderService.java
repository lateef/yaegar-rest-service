package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.LineItem;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;

@Service
public class PurchaseOrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderService.class);

    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrderService(
            ProductRepository productRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this.productRepository = productRepository;
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

    public Set<LineItem> validateLineItems(List<LineItem> lineItems, Supplier supplier, User createdBy) {
        IntStream.range(0, lineItems.size())
                .forEach(idx -> {
                    final LineItem lineItem = lineItems.get(idx);
                    lineItem.setOrder(idx + 1);

                    Product product = productRepository
                            .findById(lineItem
                                    .getProduct()
                                    .getId())
                            .orElseThrow(NullPointerException::new);

                    product.setCompany(supplier.getPrincipalCompany());
                    lineItem.setProduct(product);
                    lineItem.setSubTotal(lineItem.getUnitPrice().multiply(BigDecimal.valueOf(lineItem.getQuantity())));
                    if (Objects.isNull(lineItem.getCreatedBy())) {
                        lineItem.setCreatedBy(createdBy.getId());
                    }
                    lineItem.setUpdatedBy(createdBy.getId());
                });
        return new HashSet<>(lineItems);
    }

    public BigDecimal sumLineItemsSubTotal(Set<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::getSubTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    public List<LineItem> sortLineItemsIntoOrderedList(Set<LineItem> lineItems) {
        return lineItems.stream()
                .sorted(Comparator.comparing(LineItem::getOrder))
                .collect(Collectors.toList());
    }
}
