package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.model.SalesOrder;
import com.yaegar.yaegarrestservice.model.enums.SalesOrderState;
import com.yaegar.yaegarrestservice.repository.SalesOrderRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SalesOrderService {

    private SalesOrderRepository salesOrderRepository;
    private StockRepository stockRepository;
    private StockTransactionRepository stockTransactionRepository;

    public SalesOrderService(SalesOrderRepository salesOrderRepository,
                             StockRepository stockRepository,
                             StockTransactionRepository stockTransactionRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
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

    public Optional<SalesOrder> getSalesOrder(Long id) {
        return salesOrderRepository.findById(id);
    }

    public List<SalesOrder> getSalesOrders(Long companyId) {
        return salesOrderRepository.findAllByCompanyId(companyId);
    }

    public SalesOrder addSalesOrderActivity(SalesOrder salesOrder,
                                            SalesOrderEvent salesOrderEvent,
                                            User updatedBy) {
        salesOrder.setSalesOrderState(salesOrderEvent.getSalesOrderState());
        salesOrder.getSalesOrderActivities().add(salesOrderEvent);
        return salesOrderRepository.save(salesOrder);
    }

    @Transactional
    public SalesOrder addSalesOrderSupplyActivity(SalesOrder salesOrder,
                                                  SalesOrderEvent salesOrderEvent,
                                                  User updatedBy) {
        final SalesOrderState salesOrderState = salesOrderEvent.getSalesOrderState();
        switch (salesOrderState) {
            case GOODS_DELIVERED:
                final List<StockTransaction> stockTransactions = salesOrder.getLineItems()
                        .stream()
                        .map(lineItem -> {
                            final StockTransaction stockTransaction = new StockTransaction();
//                            stockTransaction.setInvoice(salesOrder.);
                            stockTransaction.setProduct(lineItem.getProduct());
                            stockTransaction.setQuantity(lineItem.getQuantity() * -1);
                            stockTransaction.setFromLocation(null);
                            return stockTransaction;
                        })
                        .collect(Collectors.toList());

                final List<StockTransaction> stockTransactions1 = stockTransactionRepository.saveAll(stockTransactions);

                final List<Stock> stocks = stockTransactions1.stream()
                        .map(stockTransaction -> {
                            final Stock stock = stockRepository
                                    .findByProductAndLocation(stockTransaction.getProduct(),
                                            stockTransaction.getFromLocation())
                                    .orElseThrow(NullPointerException::new);

                            final double quantity = stockTransactionRepository.findByProduct(stockTransaction.getProduct())
                                    .stream()
                                    .mapToDouble(StockTransaction::getQuantity)
                                    .sum();
                            stock.setQuantity(quantity);
                            return stock;
                        })
                        .collect(Collectors.toList());

                stockRepository.saveAll(stocks);

                break;
            default:
                break;
        }
        salesOrder.setSalesOrderState(salesOrderState);
        salesOrder.getSalesOrderActivities().add(salesOrderEvent);
        return salesOrderRepository.save(salesOrder);
    }
}
