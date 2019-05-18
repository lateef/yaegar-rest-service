package com.yaegar.yaegarrestservice.config.jwt.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Simple placeholder for info extracted from the JWT
 *
 * @author Lateef Adeniji-Adele
 */
public class JwtUserDto {

    private UUID id;

    private String username;

    private String role;

    private LocalDateTime refreshToken;

    private LocalDateTime expireToken;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(LocalDateTime refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getExpireToken() {
        return expireToken;
    }

    public void setExpireToken(LocalDateTime expireToken) {
        this.expireToken = expireToken;
    }
}