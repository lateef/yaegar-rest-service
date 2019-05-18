package com.yaegar.yaegarrestservice.config.jwt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Holds the info for a authenticated user (Principal)
 *
 * @author Lateef Adeniji-Adele
 */
public class JwtAuthenticatedUser implements UserDetails {

    private final UUID id;
    private final String username;
    private final String firstName;
    private final String token;
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtAuthenticatedUser(
            UUID id,
            String username,
            String firstName,
            String token,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.token = token;
        this.authorities = authorities;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public String getToken() {
        return token;
    }

    public String getFirstName() {
        return firstName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

}
