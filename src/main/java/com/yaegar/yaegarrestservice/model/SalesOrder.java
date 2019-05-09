package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.SalesOrderState;
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
@EqualsAndHashCode(callSuper = true, exclude = {"customer", "lineItems", "transaction", "invoices"})
@Entity
@Table
public class SalesOrder extends AbstractEntity {
    private static final long serialVersionUID = 1963042418603668211L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "number")
    private Long number;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "line_item_id", referencedColumnName = "id")
    private Set<SalesOrderLineItem> lineItems;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "paid")
    private BigDecimal paid;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transaction transaction;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "sales_order_id", referencedColumnName = "id")
    private Set<SalesInvoice> invoices;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "sales_order_event_id", referencedColumnName = "id")
    private Set<SalesOrderEvent> salesOrderEvents;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "sales_order_state", nullable = false, length = 50)
    @Enumerated(value = EnumType.STRING)
    private SalesOrderState salesOrderState;

    //TODO Payment terms
}

