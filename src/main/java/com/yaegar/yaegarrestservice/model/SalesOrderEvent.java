package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.SalesOrderEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.UUID;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class SalesOrderEvent extends AbstractEntity {
    private static final long serialVersionUID = -1257871228750276950L;

    @Column(name = "sales_order_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID salesOrderId;

    @Column(name = "sales_order_event_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private final SalesOrderEventType salesOrderEventType;

    @Column(name = "description", length = 1000)
    private final String description;
}
