package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.SalesOrderState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class SalesOrderEvent extends AbstractEntity {
    private static final long serialVersionUID = -1257871228750276950L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sales_order_event_id", nullable = false)
    private Long salesOrderEventId;

    @Column(name = "sales_order_state")
    @Enumerated(value = EnumType.STRING)
    private SalesOrderState salesOrderState;

    @Column(name = "description", length = 1000)
    private String description;
}
