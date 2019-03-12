package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.SalesOrder;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SalesOrderService extends OrderService {

    private SalesOrderRepository salesOrderRepository;

    public SalesOrderService(ProductRepository productRepository, SalesOrderRepository salesOrderRepository) {
        super(productRepository);
        this.salesOrderRepository = salesOrderRepository;
    }

    public SalesOrder saveSalesOrder(SalesOrder savedSalesOrder, User user) {
        if (Objects.isNull(savedSalesOrder.getCreatedBy())) {
            savedSalesOrder.setCreatedBy(user.getId());
        }
        savedSalesOrder.setUpdatedBy(user.getId());
        return salesOrderRepository.save(savedSalesOrder);
    }

    public Optional<SalesOrder> getSalesOrder(Long id) {
        return salesOrderRepository.findById(id);
    }

    public List<SalesOrder> getSalesOrders(Long companyId) {
        return salesOrderRepository.findAllByCustomerPrincipalCompanyId(companyId);
    }
}
