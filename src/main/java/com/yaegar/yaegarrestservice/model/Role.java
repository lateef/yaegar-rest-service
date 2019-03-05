package com.yaegar.yaegarrestservice.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table
public class Role implements GrantedAuthority {
    private static final long serialVersionUID = -4638993974570292412L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "authority", nullable = false, length = 32, unique = true)
    private String authority;

    public static final String ANONYMOUS_USER = "ANONYMOUS_USER";
    public static final String AUTHORITY_USER = "ROLE_USER";
}
