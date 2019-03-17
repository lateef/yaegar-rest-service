package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.Set;

@Data
@ToString(exclude = {"product"})
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stock",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "company_stock_id", "location_id"})})
public class Stock extends AbstractEntity {
    private static final long serialVersionUID = -8115458467683618041L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //TODO not nullable
    @Length(max = 40)
    @Column(name = "sku", unique = true, length = 40)
    private String sku;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    private Set<Account> accounts;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @Column(name = "cost_price")
    private BigDecimal costPrice;

    @Column(name = "sell_price")
    private BigDecimal sellPrice;

    @Column(name = "company_stock_id", nullable = false)
    private Long companyStockId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @Column(name = "quantity")
    private Double quantity;
}
