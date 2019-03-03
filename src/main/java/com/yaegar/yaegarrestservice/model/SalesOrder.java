package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.OrderSupplyState;
import com.yaegar.yaegarrestservice.model.enums.SalesOrderState;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "sales_order")
public class SalesOrder extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 1963042418603668211L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "sales_order_id", referencedColumnName = "id")
    private Set<LineItem> lineItems;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "received_amount")
    private BigDecimal receivedAmount;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "sales_order_state", length = 50)
    @Enumerated(value = EnumType.STRING)
    private SalesOrderState salesOrderState;

    @Column(name = "order_supply_state", length = 50)
    @Enumerated(value = EnumType.STRING)
    private OrderSupplyState orderSupplyState;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "sales_order_activity_sales_order_id", referencedColumnName = "id")
    private Set<SalesOrderActivity> salesOrderActivities;

    @Column(name = "delivery_datetime")
    private LocalDateTime deliveryDatetime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(Set<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(BigDecimal receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Set<SalesOrderActivity> getSalesOrderActivities() {
        return salesOrderActivities;
    }

    public void setSalesOrderActivities(Set<SalesOrderActivity> salesOrderActivities) {
        this.salesOrderActivities = salesOrderActivities;
    }

    public LocalDateTime getDeliveryDatetime() {
        return deliveryDatetime;
    }

    public void setDeliveryDatetime(LocalDateTime deliveryDatetime) {
        this.deliveryDatetime = deliveryDatetime;
    }
}

