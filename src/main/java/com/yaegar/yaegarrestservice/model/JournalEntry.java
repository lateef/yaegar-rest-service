package com.yaegar.yaegarrestservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.TransactionSide;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@ToString(exclude = {"transaction"})
@Data
@EqualsAndHashCode(callSuper = true, exclude = "transaction")
@Entity
public class JournalEntry extends AbstractEntity {
    private static final long serialVersionUID = 6340589739131199534L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private Account account;

    @Column(name = "transaction_side", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransactionSide transactionSide;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "entry")
    private int entry;

    @Column(name = "transaction_datetime", nullable = false)
    private LocalDateTime transactionDatetime;

    @Column(name = "short_description", length = 16, nullable = false)
    private String shortDescription;

    @Column(name = "description", length = 1000)
    private String description;
}
