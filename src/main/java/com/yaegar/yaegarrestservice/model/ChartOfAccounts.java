package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class ChartOfAccounts extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = -6037621583103591891L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_chart_of_accounts_id", referencedColumnName = "id")
    private List<Account> accounts;

    public ChartOfAccounts() {
    }

    public ChartOfAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}
