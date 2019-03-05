package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.PaymentType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment")
public class Payment extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = -2261023127157809282L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = false)
    private Transaction transaction;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "payment_type_id", nullable = false)
    private Long paymentTypeId;

    @Column(name = "description", length = 1000)
    private String description;
}
