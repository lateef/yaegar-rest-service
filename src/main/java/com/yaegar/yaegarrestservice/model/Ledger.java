package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"ledger_chart_of_accounts_id", "code"}),
                @UniqueConstraint(columnNames = {"ledger_chart_of_accounts_id", "name", "parent_uuid"})
        })
public class Ledger extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = -9030131623403189315L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", nullable = false)
    private int code;

    @NotEmpty
    @Column(name = "name", nullable = false)
    private String name;

    @Length(min = 36, max = 36)
    @Column(name = "parent_uuid", length = 36)
    private String parentUuid;

    @Column(name = "description")
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

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
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
