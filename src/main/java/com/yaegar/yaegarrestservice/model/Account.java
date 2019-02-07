package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.model.enums.ProductClassifier;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "account",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"account_chart_of_accounts_id", "code"}),
                @UniqueConstraint(columnNames = {"account_chart_of_accounts_id", "name", "account_type", "parent_id"})
        })
public class Account extends AbstractEntity implements Serializable {
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

    @Column(name = "account_type")
    @Enumerated(value = EnumType.STRING)
    private AccountType accountType;

    @Column(name = "product_classifier")
    @Enumerated(value = EnumType.STRING)
    private ProductClassifier productClassifier;

    @Column(name = "account_chart_of_accounts_id", nullable = false)
    private Long accountChartOfAccountsId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "parent")
    private boolean parent;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "deleted_datetime")
    private LocalDateTime deletedDateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public ProductClassifier getProductClassifier() {
        return productClassifier;
    }

    public void setProductClassifier(ProductClassifier productClassifier) {
        this.productClassifier = productClassifier;
    }

    public Long getAccountChartOfAccountsId() {
        return accountChartOfAccountsId;
    }

    public void setAccountChartOfAccountsId(Long accountChartOfAccountsId) {
        this.accountChartOfAccountsId = accountChartOfAccountsId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDeletedDateTime() {
        return deletedDateTime;
    }

    public void setDeletedDateTime(LocalDateTime deletedDateTime) {
        this.deletedDateTime = deletedDateTime;
    }
}
