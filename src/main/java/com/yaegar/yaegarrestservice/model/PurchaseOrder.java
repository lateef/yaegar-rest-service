package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.PaymentTerm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"supplier", "lineItems", "transaction", "invoices"})
@Entity
@Table
public class PurchaseOrder extends AbstractEntity {
    private static final long serialVersionUID = -5218638929994847147L;

    @Column(name = "number", columnDefinition = "BINARY(16)", nullable = false)
    private UUID number;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "supplier_id", referencedColumnName = "id", nullable = false)
    private Supplier supplier;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "purchase_order_id", referencedColumnName = "id")
    private Set<PurchaseOrderLineItem> lineItems;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "paid", nullable = false)
    private BigDecimal paid;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transaction transaction;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "purchase_order_id", referencedColumnName = "id")
    private Set<PurchaseInvoice> invoices;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "purchase_order_id", referencedColumnName = "id")
    private Set<PurchaseOrderEvent> purchaseOrderEvents;

    @Transient
    private String description;

    @Column(name = "payment_term", length = 50, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PaymentTerm paymentTerm;
}

