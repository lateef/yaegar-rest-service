package com.yaegar.yaegarrestservice.config.jwt;

import com.yaegar.yaegarrestservice.config.jwt.model.JwtAuthorizationToken;
import com.yaegar.yaegarrestservice.config.jwt.util.JwtTokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author Lateef Adeniji-Adele
 */
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String phoneNumber = authentication.getName();
        String password = (String) authentication.getCredentials();
        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
        if (userDetails != null && passwordEncoder.matches(password, userDetails.getPassword())) {
            return new JwtAuthorizationToken(userDetails.getUsername(), userDetails.getPassword(),
                    userDetails.getAuthorities());
        } else {
            throw new BadCredentialsException("Bad phoneNumber or password");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
