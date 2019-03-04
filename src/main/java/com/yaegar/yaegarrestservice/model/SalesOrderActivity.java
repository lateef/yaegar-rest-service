package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.OrderSupplyState;
import com.yaegar.yaegarrestservice.model.enums.SalesOrderState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class SalesOrderActivity extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = -1257871228750276950L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_number")
    private int orderNumber;

    @Column(name = "sales_order_activity_sales_order_id")
    private Long salesOrderActivitySalesOrderId;

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
}
