package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Set;

@Data
@ToString(exclude = {"principalCompany"})
@EqualsAndHashCode(callSuper = true, exclude = {"principalCompany", "supplierCompany", "products", "b2BAccount"})
@Entity
@Table(name = "supplier", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "principal_company_id"})})
public class Supplier extends AbstractEntity {
    private static final long serialVersionUID = 4695495638941520513L;

    @Length(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "principal_company_id", referencedColumnName = "id", nullable = false)
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
