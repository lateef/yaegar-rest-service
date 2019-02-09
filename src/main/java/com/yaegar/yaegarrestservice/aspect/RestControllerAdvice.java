package com.yaegar.yaegarrestservice.aspect;

import com.yaegar.yaegarrestservice.config.jwt.dto.JwtUserDto;
import com.yaegar.yaegarrestservice.config.jwt.model.JwtAuthenticationToken;
import com.yaegar.yaegarrestservice.config.jwt.util.JwtTokenValidator;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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
    private JwtTokenValidator jwtTokenValidator;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Pointcut("execution(* com.yaegar.yaegarrestservice.controller.*.*(..))")
    public void controllerService() {
    }

    @Before("controllerService()")
    public void getUser(JoinPoint joinPoint) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) Arrays.stream(joinPoint.getArgs())
                .filter(a -> a instanceof HttpServletRequest)
                .findFirst().orElse(null);

        if (httpServletRequest != null) {
            String header = httpServletRequest.getHeader(this.tokenHeader);
            if (header != null) {
                if (header.startsWith("Bearer ")) {
                    String authToken = header.substring(7);
                    JwtAuthenticationToken authRequest = new JwtAuthenticationToken(authToken);

                    JwtUserDto parsedUser = jwtTokenValidator.parseToken(authRequest.getToken());
                    if (parsedUser != null) {
                        Optional<User> userOptional = userRepository.findOptionalByPhoneNumber(parsedUser.getUsername());

                        if (userOptional.isPresent()) {
                            Arrays.stream(joinPoint.getArgs()).filter(a -> a instanceof ModelMap)
                                    .findFirst().ifPresent(a -> {
                                ((ModelMap) a).put("user", userOptional.get());
                            });
                            StringBuilder logger1 = new StringBuilder();
                            StringBuilder logger2 = new StringBuilder();
                            logger2.append(httpServletRequest.getMethod());
                            logger2.append(" request for ");
                            logger2.append(httpServletRequest.getRequestURI());
                            logger2.append(" page from ip address:");
                            logger2.append(httpServletRequest.getHeader("X-Real-IP") != null ?
                                    httpServletRequest.getHeader("X-Real-IP") : httpServletRequest.getRemoteAddr());
                            logger1.append(userOptional.get().getPhoneNumber());
                            logger1.append(" ");
//                            logger1.append(userOptional.get().getFirstName());
                            logger1.append(" ");
                            logger1.append(logger2);
                            LOGGER.info(logger1.toString());
                        }
                    }
                }
            }
        }
    }
}
