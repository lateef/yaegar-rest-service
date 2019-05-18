package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractInvoice extends AbstractEntity {
    @Column(name = "number", columnDefinition = "BINARY(16)")
    private UUID number;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "payment_due_datetime")
    private LocalDateTime paymentDueDatetime;

    @Column(name = "delivery_datetime")
    private LocalDateTime deliveryDatetime;
}
