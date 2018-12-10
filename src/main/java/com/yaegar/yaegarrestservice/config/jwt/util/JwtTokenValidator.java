package com.yaegar.yaegarrestservice.config.jwt.util;

import com.yaegar.yaegarrestservice.config.jwt.dto.JwtUserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Class validates a given token by using the secret configured in the application
 *
 * @author Lateef Adeniji-Adele
 */
@Component
public class JwtTokenValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenValidator.class);

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Tries to parse specified String as a JWT token. If successful, returns User object with username, id and role prefilled (extracted from token).
     * If unsuccessful (token is invalid or not containing all required user properties), simply returns null.
     *
     * @param token the JWT token to parse
     * @return the User object extracted from specified token or null if a token is invalid.
     */
    public JwtUserDto parseToken(String token) {
        JwtUserDto u = null;

        if (token == null) {
            return new JwtUserDto();
        }
        try {
            Claims body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            u = new JwtUserDto();
            u.setUsername(body.getSubject());
            u.setId(Long.parseLong((String) body.get("userId")));
            u.setRole((String) body.get("role"));
            Map<String, Integer> rMap = (Map<String, Integer>) body.get("refreshToken");
            Map<String, Integer> eMap = (Map<String, Integer>) body.get("expireToken");

            LocalDateTime rLocalDateTime = getLocalDateTimeFromLinkedHashMap(rMap);
            LocalDateTime eLocalDateTime = getLocalDateTimeFromLinkedHashMap(eMap);
            u.setRefreshToken(rLocalDateTime);
            u.setExpireToken(eLocalDateTime);
        } catch (JwtException e) {
            // Simply print the exception and null will be returned for the userDto
            LOGGER.error(e.getMessage());
        }
        return u;
    }

    private LocalDateTime getLocalDateTimeFromLinkedHashMap(Map<String, Integer> map) {
        Supplier<Stream<Map.Entry<String, Integer>>> streamSupplier = () -> map.entrySet().stream();
        Integer year = getIntegerFromMap(streamSupplier, "year");
        Integer month = getIntegerFromMap(streamSupplier, "monthValue");
        Integer dayOfMonth = getIntegerFromMap(streamSupplier, "dayOfMonth");
        Integer hour = getIntegerFromMap(streamSupplier, "hour");
        Integer minute = getIntegerFromMap(streamSupplier, "minute");
        Integer second = getIntegerFromMap(streamSupplier, "second");
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
    }

    private Integer getIntegerFromMap(Supplier<Stream<Map.Entry<String, Integer>>> streamSupplier, String name) {
        return streamSupplier.get()
                    .filter(e -> e.getKey().equals(name))
                    .findFirst().orElse(null).getValue();
    }
}
