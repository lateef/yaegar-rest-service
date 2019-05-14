package com.yaegar.yaegarrestservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table
public class Role implements GrantedAuthority {
    private static final long serialVersionUID = -4638993974570292412L;

    public Role(String authority) {
        this.authority = authority;
    }

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "authority", nullable = false, length = 32, unique = true)
    private String authority;

    public static final String ANONYMOUS_USER = "ANONYMOUS_USER";
    public static final String AUTHORITY_USER = "ROLE_USER";
}
