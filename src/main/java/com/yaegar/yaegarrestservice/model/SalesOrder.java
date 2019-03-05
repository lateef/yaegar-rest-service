package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.SalesOrderState;
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

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "sales_order_event_id", referencedColumnName = "id")
    private Set<SalesOrderEvent> salesOrderActivities;

    @Column(name = "delivery_datetime")
    private LocalDateTime deliveryDatetime;
}

