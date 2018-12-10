package com.yaegar.yaegarrestservice.config.jwt.model;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author Lateef Adeniji-Adele
 */
public class JwtAuthorizationToken extends UsernamePasswordAuthenticationToken {

    public JwtAuthorizationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public JwtAuthorizationToken(Object principal, Object credentials,
                                 Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
