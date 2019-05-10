package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "subscription_plan",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "credits_per_month", "duration", "price_per_month", "price_per_year", "currencyCode"})})
public class SubscriptionPlan extends AbstractEntity {
    private static final long serialVersionUID = 6096630639646459517L;

    public SubscriptionPlan(@Length(max = 256) String name, int creditsPerMonth, int duration, BigDecimal pricePerMonth, BigDecimal pricePerYear, String currencyCode) {
        this.name = name;
        this.creditsPerMonth = creditsPerMonth;
        this.duration = duration;
        this.pricePerMonth = pricePerMonth;
        this.pricePerYear = pricePerYear;
        this.currencyCode = currencyCode;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Length(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    /**
     * credits represent the number of users per plan
     */
    @Column(name = "credits_per_month", nullable = false)
    private int creditsPerMonth;

    /**
     * duration represent the number of months
     */
    @Column(name = "duration", nullable = false)
    private int duration;

    @Column(name = "price_per_month", nullable = false)
    private BigDecimal pricePerMonth;

    @Column(name = "price_per_year", nullable = false)
    private BigDecimal pricePerYear;

    @Length(max = 3)
    @Column(name = "currencyCode", nullable = false, length = 3)
    private String currencyCode;
}
