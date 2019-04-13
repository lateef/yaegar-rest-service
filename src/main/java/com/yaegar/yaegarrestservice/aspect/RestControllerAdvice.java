package com.yaegar.yaegarrestservice.aspect;

import com.yaegar.yaegarrestservice.config.jwt.model.JwtAuthenticatedUser;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Lateef Adeniji-Adele
 */
@Aspect
@Component
public class RestControllerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestControllerAdvice.class);

    @Autowired
    private UserRepository userRepository;

    @Pointcut("execution(* com.yaegar.yaegarrestservice.controller.*.*(..))")
    public void controllerService() {
    }

    @Before("controllerService()")
    public void setUser(JoinPoint joinPoint) {
        final Optional<Principal> optionalPrincipal = Arrays.stream(joinPoint.getArgs()).filter(a -> a instanceof Principal)
                .findFirst()
                .map(Principal.class::cast);

        optionalPrincipal.ifPresent(principal -> {
            final ModelMap map = (ModelMap) Arrays.stream(joinPoint.getArgs()).filter(a -> a instanceof ModelMap)
                    .findFirst()
                    .orElseThrow(NullPointerException::new);

            final JwtAuthenticatedUser jwtAuthenticatedUser =
                    (JwtAuthenticatedUser) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            final User user = userRepository.findById(jwtAuthenticatedUser.getId())
                    .orElseThrow(NullPointerException::new);
            map.put("user", user);
        });
    }
}
