package com.yaegar.yaegarrestservice.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yaegar.yaegarrestservice.model.User;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {"createdBy", "updatedBy", "updatedDatetime"}
)
@Data
public abstract class AbstractEntity implements Serializable {
    @CreatedBy
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;

    @CreatedDate
    @Column(name = "created_datetime")
    private LocalDateTime createdDatetime;

    @LastModifiedDate
    @Column(name = "updated_datetime")
    private LocalDateTime updatedDatetime;
}
