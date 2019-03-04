package com.yaegar.yaegarrestservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class Country extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = -120834228529468074L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Length(max = 55)
    @Column(name = "name", nullable = false, unique = true, length = 55)
    private String name;

    @Length(max = 55)
    @Column(name = "full_name", nullable = false, unique = true, length = 55)
    private String fullName;

    @NotEmpty
    @JsonProperty("cca2")
    @Column(name = "code", nullable = false, unique = true, length = 2)
    private String code;

    @Column(name = "iso3", nullable = false, unique = true, length = 3)
    private String iso3;

    @NotEmpty
    @Column(name = "continent_code", nullable = false, unique = true, length = 2)
    private String continentCode;

    public Country(@Length(max = 32) String name, @NotEmpty String code, @NotEmpty String continentCode) {
        this.name = name;
        this.code = code;
        this.continentCode = continentCode;
    }
}
