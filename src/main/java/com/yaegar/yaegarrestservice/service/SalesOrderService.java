package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.SalesOrderEvent;
import com.yaegar.yaegarrestservice.model.SalesOrderLineItem;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.SalesInvoiceLineItem;
import com.yaegar.yaegarrestservice.model.SalesOrder;
import com.yaegar.yaegarrestservice.model.enums.SalesOrderEventType;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.SalesOrderRepository;
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
public class SalesOrderService {

    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;

    public SalesOrder saveSalesOrder(SalesOrder savedSalesOrder) {
        return salesOrderRepository.save(savedSalesOrder);
    }

    public Optional<SalesOrder> getSalesOrder(UUID id) {
        return salesOrderRepository.findById(id);
    }

    public List<SalesOrder> getSalesOrders(UUID companyId) {
        return salesOrderRepository.findAllByCustomerPrincipalCompanyId(companyId);
    }

    public Set<SalesOrderLineItem> validateOrderLineItems(Set<SalesOrderLineItem> lineItems) {
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

    public Set<SalesInvoiceLineItem> validateInvoiceLineItems(List<SalesInvoiceLineItem> lineItems) {
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

    public BigDecimal sumOrderLineItemsSubTotal(Set<SalesOrderLineItem> lineItems) {
        return lineItems.stream()
                .map(SalesOrderLineItem::getSubTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    public BigDecimal sumInvoiceLineItemsSubTotal(Set<SalesInvoiceLineItem> lineItems) {
        return lineItems.stream()
                .map(SalesInvoiceLineItem::getSubTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    public SalesOrder addEvent(SalesOrder salesOrder, String description, SalesOrderEventType orderEventType) {
        final Set<SalesOrderEvent> salesOrderEvents = salesOrder.getSalesOrderEvents();
        final SalesOrderEvent salesOrderEvent = new SalesOrderEvent(orderEventType, description);
        salesOrderEvents.add(salesOrderEvent);
        salesOrder.setSalesOrderEvents(salesOrderEvents);
        return salesOrder;
    }
}
