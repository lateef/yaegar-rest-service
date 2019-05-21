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
@EqualsAndHashCode(callSuper = true, exclude = {"customer", "lineItems", "transaction", "invoices"})
@Entity
@Table
public class SalesOrder extends AbstractEntity {
    private static final long serialVersionUID = 1963042418603668211L;

    @Column(name = "number", columnDefinition = "BINARY(16)", nullable = false)
    private UUID number;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "sales_order_id", referencedColumnName = "id")
    private Set<SalesOrderLineItem> lineItems;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "paid", nullable = false)
    private BigDecimal paid;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transaction transaction;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "sales_order_id", referencedColumnName = "id")
    private Set<SalesInvoice> invoices;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "sales_order_id", referencedColumnName = "id")
    private Set<SalesOrderEvent> salesOrderEvents;

    @Transient
    private String description;

    @Column(name = "payment_term", length = 50, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PaymentTerm paymentTerm;
}

