package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.OrderSupplyState;
import com.yaegar.yaegarrestservice.model.enums.SalesOrderState;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_order_activity")
public class SalesOrderActivity extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = -1257871228750276950L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sales_order_state")
    @Enumerated(value = EnumType.STRING)
    private SalesOrderState salesOrderState;

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

    public SalesOrderState getSalesOrderState() {
        return salesOrderState;
    }

    public void setSalesOrderState(SalesOrderState salesOrderState) {
        this.salesOrderState = salesOrderState;
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
