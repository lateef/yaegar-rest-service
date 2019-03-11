package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Invoice;
import com.yaegar.yaegarrestservice.model.LineItem;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.PurchaseOrderEvent;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.model.StockTransaction;
import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import com.yaegar.yaegarrestservice.repository.PurchaseOrderRepository;
import com.yaegar.yaegarrestservice.repository.StockRepository;
import com.yaegar.yaegarrestservice.repository.StockTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.GOODS_RECEIVED;
import static java.math.BigDecimal.ZERO;

@Service
public class PurchaseOrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderService.class);

    private ProductRepository productRepository;
    private PurchaseOrderRepository purchaseOrderRepository;
    private StockRepository stockRepository;
    private StockTransactionRepository stockTransactionRepository;

    public PurchaseOrderService(
            ProductRepository productRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            StockRepository stockRepository,
            StockTransactionRepository stockTransactionRepository
    ) {
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
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

    public PurchaseOrder saveInvoices(PurchaseOrder purchaseOrder, Set<Invoice> invoices, User updatedBy) {
        //TODO confirm payment was paid before setting PAYMENT and update user on only updated payments
        purchaseOrder.setInvoices(invoices);
        purchaseOrder.setPurchaseOrderState(GOODS_RECEIVED);
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder addPurchaseOrderSupplyActivity(PurchaseOrder purchaseOrder,
                                                        PurchaseOrderEvent purchaseOrderEvent,
                                                        User updatedBy) {
        final PurchaseOrderState purchaseOrderState = purchaseOrderEvent.getPurchaseOrderState();
        switch (purchaseOrderState) {
            case GOODS_RECEIVED:
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
        purchaseOrder.setPurchaseOrderState(purchaseOrderState);
//        purchaseOrder.getPurchaseOrderActivities().add(purchaseOrderEvent);
        return purchaseOrderRepository.save(purchaseOrder);
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
