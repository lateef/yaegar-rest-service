package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "phone")
public class Phone extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = -7901958678943948605L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotEmpty
    @Column(name = "code", length = 3, nullable = false)
    private String code;

    @NotEmpty
    @Column(name = "number", unique = true, length = 15, nullable = false)
    private String number;

    @NotEmpty
    @Column(name = "primary", nullable = false)
    private boolean primary;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return Objects.equals(code, phone.code) &&
                Objects.equals(number, phone.number);
    }

    @Override
    public int hashCode() {

        return Objects.hash(code, number);
    }

    @Override
    public String toString() {
        return "Phone{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
