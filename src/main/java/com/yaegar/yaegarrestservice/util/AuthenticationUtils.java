package com.yaegar.yaegarrestservice.util;

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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Lateef Adeniji-Adele
 */
public class AuthenticationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationUtils.class);

    private static ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();

    private static UserRepository userRepository = (UserRepository) ctx.getBean("userRepository");

    private AuthenticationUtils() {
    }

    public static JwtAuthenticatedUser jwtAuthenticatedUser(JwtUserDto parsedUser,
                                                            Collection<GrantedAuthority> grantedAuthorities) {
        Optional<User> userOptional = userRepository.findOptionalByPhoneNumber(parsedUser.getUsername());

        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("User does not exist");
        }

        User user = userOptional.get();

        JwtUserDto jwtUserDto = new JwtUserDto();
        jwtUserDto.setId(user.getUserId());
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

        return new JwtAuthenticatedUser(user.getUserId(), user.getPhoneNumber(), user.getFirstName(),
                token, grantedAuthorities);
    }

    public static HttpHeaders getAuthenticatedUser(User user) {
        JwtUserDto jwtUserDto = new JwtUserDto();
        jwtUserDto.setUsername(user.getPhoneNumber());
        JwtAuthenticatedUser jwtAuthenticatedUser = jwtAuthenticatedUser(jwtUserDto,
                user.getAuthorities());

        HttpHeaders headers = new HttpHeaders();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            headers.set("access_token", objectMapper.writeValueAsString(jwtAuthenticatedUser));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return headers;
    }
}
