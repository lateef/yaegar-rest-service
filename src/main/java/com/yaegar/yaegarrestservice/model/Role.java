package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class Role extends AbstractEntity implements GrantedAuthority {
    private static final long serialVersionUID = -4638993974570292412L;

    @Column(name = "authority", nullable = false, length = 68, unique = true)
    private final String authority;

    public static final String ANONYMOUS_USER = "ANONYMOUS_USER";
    public static final String AUTHORITY_USER = "ROLE_USER";
}
