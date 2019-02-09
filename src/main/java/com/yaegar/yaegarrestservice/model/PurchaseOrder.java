package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.OrderSupplyState;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "purchase_order")
public class PurchaseOrder extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = -5218638929994847147L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "supplier_id", referencedColumnName = "id")
    private Supplier supplier;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "line_item_purchase_order_id", referencedColumnName = "id")
    private Set<LineItem> lineItems;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "purchase_order_state", length = 50)
    @Enumerated(value = EnumType.STRING)
    private PurchaseOrderState purchaseOrderState;

    @Column(name = "order_supply_state", length = 50)
    @Enumerated(value = EnumType.STRING)
    private OrderSupplyState orderSupplyState;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "purchase_order_activity_purchase_order_id", referencedColumnName = "id")
    private List<PurchaseOrderActivity> purchaseOrderActivities;

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

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
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

    public String getDescription() {
        return description;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PurchaseOrderActivity> getPurchaseOrderActivities() {
        return purchaseOrderActivities;
    }

    public void setPurchaseOrderActivities(List<PurchaseOrderActivity> purchaseOrderActivities) {
        this.purchaseOrderActivities = purchaseOrderActivities;
    }

    public LocalDateTime getDeliveryDatetime() {
        return deliveryDatetime;
    }

    public void setDeliveryDatetime(LocalDateTime deliveryDatetime) {
        this.deliveryDatetime = deliveryDatetime;
    }
}

