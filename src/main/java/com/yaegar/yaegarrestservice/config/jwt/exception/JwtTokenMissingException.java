package com.yaegar.yaegarrestservice.config.jwt.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author Lateef Adeniji-Adele
 */
public class JwtTokenMissingException extends AuthenticationException {

    public JwtTokenMissingException(String msg) {
        super(msg);
    }
}
