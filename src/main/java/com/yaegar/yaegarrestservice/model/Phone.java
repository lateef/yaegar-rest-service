package com.yaegar.yaegarrestservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.util.Objects;

@ToString
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table
public class Phone extends AbstractEntity {
    private static final long serialVersionUID = -7901958678943948605L;

    @NotEmpty
    @JsonProperty("callingCode")
    @Column(name = "code", length = 3, nullable = false)
    private String code;

    @NotEmpty
    @Column(name = "number", unique = true, length = 17, nullable = false)
    private String number;

    @Column(name = "principal", nullable = false)
    private boolean principal;

    @Column(name = "confirmation_code", length = 6)
    private String confirmationCode;

    @Column(name = "confirmed")
    private boolean confirmed;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;

    public Phone(@NotEmpty String code, @NotEmpty String number, boolean principal, Country country) {
        this.code = code;
        this.number = number;
        this.principal = principal;
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
}
