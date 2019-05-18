package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Entity
@Table(name = "subscription_plan",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "credits_per_month", "duration", "price_per_month", "price_per_year", "currency_code"})})
public class SubscriptionPlan extends AbstractEntity {
    private static final long serialVersionUID = 6096630639646459517L;

    @Length(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private final String name;

    /**
     * credits represent the number of users per plan
     */
    @Column(name = "credits_per_month", nullable = false)
    private final int creditsPerMonth;

    /**
     * duration represent the number of months
     */
    @Column(name = "duration", nullable = false)
    private final int duration;

    @Column(name = "price_per_month", nullable = false)
    private final BigDecimal pricePerMonth;

    @Column(name = "price_per_year", nullable = false)
    private final BigDecimal pricePerYear;

    @Length(max = 3)
    @Column(name = "currency_code", nullable = false, length = 3)
    private final String currencyCode;
}
