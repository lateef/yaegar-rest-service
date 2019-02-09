package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.OrderSupplyState;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_order_activity")
public class PurchaseOrderActivity extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 7720535667661027391L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_number")
    private int orderNumber;

    @Column(name = "purchase_order_activity_purchase_order_id")
    private Long purchaseOrderActivityPurchaseOrderId;

    @Column(name = "purchase_order_state")
    @Enumerated(value = EnumType.STRING)
    private PurchaseOrderState purchaseOrderState;

    @Column(name = "order_supply_state")
    @Enumerated(value = EnumType.STRING)
    private OrderSupplyState orderSupplyState;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "delivery_datetime")
    private LocalDateTime deliveryDatetime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getPurchaseOrderActivityPurchaseOrderId() {
        return purchaseOrderActivityPurchaseOrderId;
    }

    public void setPurchaseOrderActivityPurchaseOrderId(Long purchaseOrderActivityPurchaseOrderId) {
        this.purchaseOrderActivityPurchaseOrderId = purchaseOrderActivityPurchaseOrderId;
    }

    public PurchaseOrderState getPurchaseOrderState() {
        return purchaseOrderState;
    }

    public void setPurchaseOrderState(PurchaseOrderState purchaseOrderState) {
        this.purchaseOrderState = purchaseOrderState;
    }

    public OrderSupplyState getOrderSupplyState() {
        return orderSupplyState;
    }

    public void setOrderSupplyState(OrderSupplyState orderSupplyState) {
        this.orderSupplyState = orderSupplyState;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDeliveryDatetime() {
        return deliveryDatetime;
    }

    public void setDeliveryDatetime(LocalDateTime deliveryDatetime) {
        this.deliveryDatetime = deliveryDatetime;
    }
}
