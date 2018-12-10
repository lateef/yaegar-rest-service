package com.yaegar.yaegarrestservice.config.jwt.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;

/**
 * @author Lateef Adeniji-Adele
 */
public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private String token;

    public JwtAuthenticationToken(String token) throws IOException {
        super(null, null);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(token);
        this.token = node.path("token").textValue();
    }

    public String getToken() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
