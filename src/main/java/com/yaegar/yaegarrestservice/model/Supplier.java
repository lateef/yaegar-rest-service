package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.Set;

@ToString(exclude = {"principalCompany"})
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "supplier",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "principal_company_id"})})
public class Supplier extends AbstractEntity {
    private static final long serialVersionUID = 4695495638941520513L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Length(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "principal_company_id", referencedColumnName = "id")
    private Company principalCompany;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "supplier_company_id", referencedColumnName = "id")
    private Company supplierCompany;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private Set<Product> products;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "b2b_account_id", referencedColumnName = "id")
    private B2bAccount b2BAccount;
}
