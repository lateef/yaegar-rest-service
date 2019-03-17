package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.GlobalTradeItemNumberType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Product extends AbstractEntity {
    private static final long serialVersionUID = 9131433206492217756L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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
    private List<ProductVariant> productVariants;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @Column(name = "gtin_type", length = 7)
    @Enumerated(value = EnumType.STRING)
    private GlobalTradeItemNumberType globalTradeItemNumberType;

    @Column(name = "gtin", length = 14)
    private String globalTradeItemNumber;

    @Column(name = "deleted_datetime")
    private LocalDateTime deletedDateTime;
}
