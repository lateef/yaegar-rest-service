package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderEventType;
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
public class PurchaseOrderEvent extends AbstractEntity {
    private static final long serialVersionUID = 7720535667661027391L;

    @Column(name = "purchase_order_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID purchaseOrderId;

    @Column(name = "purchase_order_event_type")
    @Enumerated(value = EnumType.STRING)
    private final PurchaseOrderEventType purchaseOrderEventType;

    @Column(name = "description", length = 1000)
    private final String description;
}
