package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "principal_company_id"})})
public class Customer extends AbstractEntity {
    private static final long serialVersionUID = 9108589308270906156L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

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
