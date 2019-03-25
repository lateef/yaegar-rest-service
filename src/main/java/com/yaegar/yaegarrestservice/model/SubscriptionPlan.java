package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class SubscriptionPlan extends AbstractEntity {
    private static final long serialVersionUID = 6096630639646459517L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Length(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "credits_per_month", nullable = false, length = 256)
    private String creditsPerMonth;

    @Column(name = "price_per_month", nullable = false)
    private Double pricePerMonth;

    @Column(name = "price_per_year", nullable = false)
    private Double pricePerYear;
}
