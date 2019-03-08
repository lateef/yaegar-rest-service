package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.TransactionSide;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class JournalEntry extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 6340589739131199534L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    @Column(name = "transaction_side", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransactionSide transactionSide;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "entry")
    private int entry;

    @Column(name = "transaction_datetime")
    private LocalDateTime transactionDatetime;

    @Column(name = "description", length = 1000)
    private String description;
}
