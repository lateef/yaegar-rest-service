package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class PurchaseOrder extends AbstractEntity {
    private static final long serialVersionUID = -5218638929994847147L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "number")
    private Long number;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "supplier_id", referencedColumnName = "id")
    private Supplier supplier;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "line_item_purchase_order_id", referencedColumnName = "id")
    private Set<LineItem> lineItems;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @OneToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transaction transaction;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "invoice_purchase_order_id", referencedColumnName = "id")
    private Set<Invoice> invoices;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "purchase_order_event_id", referencedColumnName = "id")
    private Set<PurchaseOrderEvent> purchaseOrderEvents;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "purchase_order_state", nullable = false, length = 50)
    @Enumerated(value = EnumType.STRING)
    private PurchaseOrderState purchaseOrderState;

    //TODO Payment terms
}

