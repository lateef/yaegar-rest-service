package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.LocationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Location extends AbstractEntity {
    private static final long serialVersionUID = 98946293342395817L;

    @Length(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Length(max = 10)
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "location_type", length = 50)
    @Enumerated(value = EnumType.STRING)
    private LocationType locationType;
}
