package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Supplier",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"Name", "company_id"})})
public class Supplier extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 4695495638941520513L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Length(max = 256)
    @Column(name = "Name", nullable = false, length = 256)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "company_supplier_id", referencedColumnName = "id")
    private Company companySupplier;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_supplier_id", referencedColumnName = "id")
    private Company userSupplier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Company getCompanySupplier() {
        return companySupplier;
    }

    public void setCompanySupplier(Company companySupplier) {
        this.companySupplier = companySupplier;
    }

    public Company getUserSupplier() {
        return userSupplier;
    }

    public void setUserSupplier(Company userSupplier) {
        this.userSupplier = userSupplier;
    }
}
