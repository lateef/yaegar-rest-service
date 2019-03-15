package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.TransactionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Transaction extends AbstractEntity {
    private static final long serialVersionUID = -6306868349093193363L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Set<JournalEntry> journalEntries;

    @Column(name = "transaction_type")
    @Enumerated(value = EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "transaction_type_id")
    private Long transactionTypeId;

    @Transient
    private Set<Account> accounts;
}
