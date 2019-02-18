package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.model.enums.OrderSupplyState;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    private PurchaseOrderRepository purchaseOrderRepository;
    private StockRepository stockRepository;
    private StockTransactionRepository stockTransactionRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                StockRepository stockRepository,
                                StockTransactionRepository stockTransactionRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
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

    @Transactional
    public PurchaseOrder addPurchaseOrderSupplyActivity(PurchaseOrder purchaseOrder,
                                                        PurchaseOrderActivity purchaseOrderActivity,
                                                        User updatedBy) {
        final OrderSupplyState orderSupplyState = purchaseOrderActivity.getOrderSupplyState();
        switch (orderSupplyState) {
            case NO_SUPPLY:
                break;
            case PART_SUPPLY:
                break;
            case FULL_SUPPLY:
                final List<StockTransaction> stockTransactions = purchaseOrder.getLineItems()
                        .stream()
                        .map(lineItem -> {
                            final StockTransaction stockTransaction = new StockTransaction();
                            stockTransaction.setPurchaseOrder(purchaseOrder);
                            stockTransaction.setProduct(lineItem.getProduct());
                            stockTransaction.setQuantity(lineItem.getQuantity());
                            stockTransaction.setToLocation(null);
                            return stockTransaction;
                        })
                        .collect(Collectors.toList());

                final List<StockTransaction> stockTransactions1 = stockTransactionRepository.saveAll(stockTransactions);

                final List<Stock> stocks = stockTransactions1.stream()
                        .map(stockTransaction -> {
                            final Stock stock = stockRepository
                                    .findByProductAndLocation(stockTransaction.getProduct(),
                                            stockTransaction.getToLocation())
                                    .orElse(new Stock());

                            if (stock.getId() != null) {
                                final double quantity = stockTransactionRepository.findByProduct(stockTransaction.getProduct())
                                        .stream()
                                        .mapToDouble(StockTransaction::getQuantity)
                                        .sum();
                                stock.setQuantity(quantity);
                            } else {
                                stock.setProduct(stockTransaction.getProduct());
                                stock.setLocation(stockTransaction.getToLocation());
                                stock.setQuantity(stockTransaction.getQuantity());
                            }
                            return stock;
                        })
                        .collect(Collectors.toList());

                stockRepository.saveAll(stocks);

                break;
            default:
                break;
        }
        purchaseOrder.setOrderSupplyState(orderSupplyState);
        purchaseOrder.getPurchaseOrderActivities().add(purchaseOrderActivity);
        return purchaseOrderRepository.save(purchaseOrder);
    }
}
