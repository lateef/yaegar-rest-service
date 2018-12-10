package com.yaegar.yaegarrestservice.model;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author Lateef Adeniji-Adele
 */
@Entity
@Table(name = "Role")
public class Role implements GrantedAuthority {
    private static final long serialVersionUID = -4638993974570292412L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private long roleId;

    @Column(name = "Authority", nullable = false, length = 32, unique = true)
    private String authority;

    public static final String AUTHORITY_USER = "ROLE_USER";

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getRoleId() {
        return roleId;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return authority.equals(role.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authority);
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", authority='" + authority + '\'' +
                '}';
    }
}
