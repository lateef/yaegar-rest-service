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
    @JoinColumn(name = "ledger_chart_of_accounts_id", referencedColumnName = "id")
    private List<Ledger> ledgers;

    public ChartOfAccounts() {
    }

    public ChartOfAccounts(List<Ledger> ledgers) {
        this.ledgers = ledgers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Ledger> getLedgers() {
        return ledgers;
    }

    public void setLedgers(List<Ledger> ledgers) {
        this.ledgers = ledgers;
    }
}
