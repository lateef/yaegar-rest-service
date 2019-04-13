package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "accounts")
@Entity
public class ChartOfAccounts extends AbstractEntity {
    private static final long serialVersionUID = -6037621583103591891L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(mappedBy = "chartOfAccounts", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Account> accounts;
}
