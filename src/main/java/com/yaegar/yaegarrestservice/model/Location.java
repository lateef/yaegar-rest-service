package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import com.yaegar.yaegarrestservice.model.enums.LocationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "location", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "company_id"}),
        @UniqueConstraint(columnNames = {"code", "company_id"})
})
public class Location extends AbstractEntity {
    private static final long serialVersionUID = 98946293342395817L;

    @Length(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Length(max = 36)
    @Column(name = "code", nullable = false, length = 36)
    private String code;

    @Column(name = "location_type", length = 50, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private LocationType locationType;
}
