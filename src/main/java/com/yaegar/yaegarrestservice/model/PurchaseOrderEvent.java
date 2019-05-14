package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class PurchaseOrderEvent extends AbstractEntity {
    private static final long serialVersionUID = 7720535667661027391L;

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "purchase_order_event_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID purchaseOrderEventId;

    @Column(name = "purchase_order_state")
    @Enumerated(value = EnumType.STRING)
    private PurchaseOrderState purchaseOrderState;

    @Column(name = "description", length = 1000)
    private String description;
}
