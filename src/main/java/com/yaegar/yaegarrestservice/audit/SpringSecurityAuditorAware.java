package com.yaegar.yaegarrestservice.audit;

import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<User> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Unable to get a CurrentAuditor: no user signed in");
        }
        if (authentication.getName().equals("anonymousUser") || authentication.getName().equals("admin")) {
            return Optional.empty();
        } else {
            return userRepository.findOptionalByPhoneNumber(authentication.getName());
        }
    }
}
