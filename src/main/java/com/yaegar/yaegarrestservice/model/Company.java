package com.yaegar.yaegarrestservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
public class Company extends AbstractEntity {
    private static final long serialVersionUID = -2248566160300140508L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private Set<User> owners = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private Set<User> employees = new HashSet<>();

    @NotNull
    @Length(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "chart_of_accounts_id", referencedColumnName = "id")
    private ChartOfAccounts chartOfAccounts;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinColumn(name = "company_stock_id", referencedColumnName = "id")
    private Set<Stock> stocks;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinColumn(name = "location_company_id", referencedColumnName = "id")
    private List<Location> locations;

    public Company(@Length(max = 256) String name) {
        this.name = name;
    }
}
