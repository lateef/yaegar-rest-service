package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.LineItem;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ZERO;

@Service
public abstract class OrderService {
    private final ProductRepository productRepository;

    protected OrderService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Set<LineItem> validateLineItems(List<LineItem> lineItems, Company company, User createdBy) {
        IntStream.range(0, lineItems.size())
                .forEach(idx -> {
                    final LineItem lineItem = lineItems.get(idx);
                    lineItem.setEntry(idx + 1);

                    Product product = productRepository
                            .findById(lineItem
                                    .getProduct()
                                    .getId())
                            .orElseThrow(NullPointerException::new);

                    product.setCompany(company);
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
                .sorted(Comparator.comparing(LineItem::getEntry))
                .collect(Collectors.toList());
    }
}
