package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractLineItem extends AbstractEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @NotNull
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private double quantity;

    @NotNull
    @Column(name = "sub_total", nullable = false)
    private BigDecimal subTotal;
}
