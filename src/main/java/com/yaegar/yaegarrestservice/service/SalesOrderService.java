package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.SalesOrder;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesOrderService {

    private SalesOrderRepository salesOrderRepository;

    public SalesOrderService(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
    }

    public SalesOrder addSalesOrder(SalesOrder salesOrder, User createdBy) {
        salesOrder.getLineItems().forEach(lineItem -> {
            lineItem.setCreatedBy(createdBy.getId());
            lineItem.setUpdatedBy(createdBy.getId());
        });
        salesOrder.setCreatedBy(createdBy.getId());
        salesOrder.setUpdatedBy(createdBy.getId());
        return salesOrderRepository.save(salesOrder);
    }

    public List<SalesOrder> getSalesOrders(Long companyId) {
        return salesOrderRepository.findAllByCompanyId(companyId);
    }
}
