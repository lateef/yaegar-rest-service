package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.GlobalTradeItemNumberType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Product extends AbstractEntity {
    private static final long serialVersionUID = 9131433206492217756L;

    @Length(max = 128)
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Length(max = 128)
    @Column(name = "manufacturer", nullable = false, length = 128)
    private String manufacturer;

    @Length(max = 512)
    @Column(name = "title", nullable = false, length = 512)
    private String title;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ProductVariant> productVariants;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "gtin_type", length = 7)
    @Enumerated(value = EnumType.STRING)
    private GlobalTradeItemNumberType globalTradeItemNumberType;

    @Column(name = "gtin", length = 14)
    private String globalTradeItemNumber;

    @Column(name = "product_tracking_id", unique = true)
    private UUID productTrackingId;
}
