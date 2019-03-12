package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class StockTransaction extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = -7576476879020788149L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id")
    private Invoice  invoice;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @Column(name = "quantity")
    private double quantity;
}
