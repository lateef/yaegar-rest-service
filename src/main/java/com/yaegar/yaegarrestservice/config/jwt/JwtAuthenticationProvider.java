package com.yaegar.yaegarrestservice.config.jwt;

import com.yaegar.yaegarrestservice.config.jwt.dto.JwtUserDto;
import com.yaegar.yaegarrestservice.config.jwt.exception.JwtTokenMalformedException;
import com.yaegar.yaegarrestservice.config.jwt.exception.JwtTokenMissingException;
import com.yaegar.yaegarrestservice.config.jwt.model.JwtAuthenticationToken;
import com.yaegar.yaegarrestservice.config.jwt.util.JwtTokenValidator;
import com.yaegar.yaegarrestservice.provider.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    @Autowired
    private Authenticator authenticator;

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        String token = jwtAuthenticationToken.getToken();
        if (token == null) {
            throw new JwtTokenMissingException("JWT token not found");
        }
        JwtUserDto parsedUser = jwtTokenValidator.parseToken(token);
        if (parsedUser == null) {
            throw new JwtTokenMalformedException("JWT token is not valid");
        }

        List<GrantedAuthority> authorityList = AuthorityUtils.commaSeparatedStringToAuthorityList(parsedUser.getRole());
        return authenticator.jwtAuthenticatedUser(parsedUser, authorityList);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
