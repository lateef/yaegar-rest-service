package com.yaegar.yaegarrestservice.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaegar.yaegarrestservice.config.jwt.dto.JwtUserDto;
import com.yaegar.yaegarrestservice.config.jwt.model.JwtAuthenticatedUser;
import com.yaegar.yaegarrestservice.config.jwt.util.JwtTokenGenerator;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Lateef Adeniji-Adele
 */
@Component
public class Authenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);

    private static ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();

    private static UserRepository userRepository = (UserRepository) ctx.getBean("userRepository");

    public  JwtAuthenticatedUser jwtAuthenticatedUser(JwtUserDto parsedUser,
                                                            Collection<GrantedAuthority> grantedAuthorities) {
        Optional<User> userOptional = userRepository.findOptionalByPhoneNumber(parsedUser.getUsername());

        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("User does not exist");
        }

        User user = userOptional.get();

        JwtUserDto jwtUserDto = new JwtUserDto();
        jwtUserDto.setId(user.getId());
        jwtUserDto.setUsername(user.getPhoneNumber());
        String roles = grantedAuthorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        jwtUserDto.setRole(roles);
        jwtUserDto.setRefreshToken(LocalDateTime.now());

        if (parsedUser.getExpireToken() == null) {
            jwtUserDto.setExpireToken(LocalDateTime.now());
        }

        String token = JwtTokenGenerator.generateToken(jwtUserDto, "!r4g34Y!");

        return new JwtAuthenticatedUser(user.getId(), user.getPhoneNumber(), user.getFirstName(),
                token, grantedAuthorities);
    }

    public HttpHeaders getAuthenticatedUser(User user) {
        JwtUserDto jwtUserDto = new JwtUserDto();
        JwtAuthenticatedUser jwtAuthenticatedUser;
        HttpHeaders headers = new HttpHeaders();

        try {
            jwtUserDto.setUsername(user.getPhoneNumber());
            jwtAuthenticatedUser = jwtAuthenticatedUser(jwtUserDto, user.getAuthorities());
        } catch (NullPointerException e) {
            LOGGER.error("No user found");
            headers.set("access_token", null);
            return headers;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            headers.set("access_token", objectMapper.writeValueAsString(jwtAuthenticatedUser));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return headers;
    }
}
