package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class ProductVariant extends AbstractEntity {
    private static final long serialVersionUID = 7870120972711244542L;

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "attribute")
    private String attribute;

    @Column(name = "value", nullable = false)
    private String value;
}
