package com.yaegar.yaegarrestservice.config.jwt.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when token cannot be parsed
 * @author Lateef Adeniji-Adele
 */
public class JwtTokenMalformedException extends AuthenticationException {

    public JwtTokenMalformedException(String msg) {
        super(msg);
    }
}
