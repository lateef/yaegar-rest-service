package com.yaegar.yaegarrestservice.config.jwt.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Lateef Adeniji-Adele
 */
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
//        JwtUserDto jwtUserDto = new JwtUserDto();
//        jwtUserDto.setId(0L);
//        jwtUserDto.setUsername(authentication.getName());
//        String roles = authentication.getAuthorities()
//                .stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(","));
//        jwtUserDto.setRole(roles);
//        String token = JwtTokenGenerator.generateToken(jwtUserDto, "rk1rKsp0T");
//        ObjectMapper objectMapper = new ObjectMapper();
//        JwtAuthenticatedUser jwtAuthenticatedUser = new JwtAuthenticatedUser(null, authentication.getName(), token,
//                authentication.getAuthorities());
//        response.getWriter().print(objectMapper.writeValueAsString(jwtAuthenticatedUser));
        response.flushBuffer();
    }
}

