package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

    public ChartOfAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}
