package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "customer",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "company_id"})})
public class Customer extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 9108589308270906156L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Length(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    private Company company;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "company_customer_id", referencedColumnName = "id")
    private Company companyCustomer;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_customer_id", referencedColumnName = "id")
    private Company userCustomer;

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

    public Company getCompanyCustomer() {
        return companyCustomer;
    }

    public void setCompanyCustomer(Company companyCustomer) {
        this.companyCustomer = companyCustomer;
    }

    public Company getUserCustomer() {
        return userCustomer;
    }

    public void setUserCustomer(Company userCustomer) {
        this.userCustomer = userCustomer;
    }
}
