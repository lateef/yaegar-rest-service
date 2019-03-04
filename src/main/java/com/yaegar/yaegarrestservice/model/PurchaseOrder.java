package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.OrderSupplyState;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
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
    private Set<PurchaseOrderActivity> purchaseOrderActivities;

    @Column(name = "delivery_datetime")
    private LocalDateTime deliveryDatetime;
}

