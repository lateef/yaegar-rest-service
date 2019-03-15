package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.AccountCategory;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"account_chart_of_accounts_id", "code"}),
                @UniqueConstraint(columnNames = {"account_chart_of_accounts_id", "name", "account_type", "account_category"})
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class Account extends AbstractEntity {
    private static final long serialVersionUID = -9030131623403189315L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", nullable = false)
    private int code;

    @NotEmpty
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "account_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AccountType accountType;

    @Column(name = "account_category")
    @Enumerated(value = EnumType.STRING)
    private AccountCategory accountCategory;

    @Column(name = "account_chart_of_accounts_id", nullable = false)
    private Long accountChartOfAccountsId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "parent")
    private boolean parent;

    @Column(name = "enable")
    private boolean enable;

    @Column(name = "can_delete")
    private boolean canDelete;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "deleted_datetime")
    private LocalDateTime deletedDateTime;

    @Column(name = "day_total")
    private BigDecimal dayTotal;

    @Column(name = "week_to_date_total")
    private BigDecimal weekToDateTotal;

    @Column(name = "month_to_date_total")
    private BigDecimal monthToDateTotal;

    @Column(name = "year_to_date_total")
    private BigDecimal yearToDateTotal;

    @Column(name = "last_one_year_total")
    private BigDecimal lastOneYearTotal;
}
