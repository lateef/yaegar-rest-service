package com.yaegar.yaegarrestservice.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yaegar.yaegarrestservice.model.User;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {"createdBy", "updatedBy", "createdDateTime", "updatedDateTime"}
)
public abstract class AbstractEntity {
    @NotEmpty
    @Length(min = 36, max = 36)
    @Column(name = "Uuid", unique = true, nullable = false, length = 36)
    private String uuid;

    @CreatedBy
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "CreatedByUserID", referencedColumnName = "UserID")
    private User createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "UpdatedByUserID", referencedColumnName = "UserID")
    private User updatedBy;

    @CreatedDate
    @Column(name = "CreationDateTime")
    private LocalDateTime createdDateTime;

    @LastModifiedDate
    @Column(name = "UpdatedDateTime")
    private LocalDateTime updatedDateTime;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public LocalDateTime getUpdatedDateTime() {
        return updatedDateTime;
    }

    public void setUpdatedDateTime(LocalDateTime updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
    }
}
