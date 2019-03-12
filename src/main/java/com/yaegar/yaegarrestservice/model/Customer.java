package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "principal_company_id"})})
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
    @JoinColumn(name = "principal_company_id", referencedColumnName = "id")
    private Company principalCompany;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_company_id", referencedColumnName = "id")
    private Company customerCompany;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "b2b_account_id", referencedColumnName = "id")
    private B2bAccount b2BAccount;
}
