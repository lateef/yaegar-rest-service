package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.config.ConfigHolder;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lateef Adeniji-Adele
 */
@Transactional(readOnly = true)
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private ConfigHolder configHolder;

    private UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(ConfigHolder configHolder, UserRepository userRepository) {
        this.configHolder = configHolder;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
            User user = userRepository.findOptionalByPhoneNumber(phoneNumber).orElseThrow(
                () -> new UsernameNotFoundException(phoneNumber + " not found"));

        if (user.getFailedLoginAttempts() >= configHolder.getMaxLoginAttempts()) {
            user.setAccountNonLocked(false);
        } else {
            user.setAccountNonLocked(true);
        }

        return new org.springframework.security.core.userdetails.User(user.getPhoneNumber(), user.getPassword(),
                user.isEnabled(), user.isAccountNonExpired(), user.isCredentialsNonExpired(),
                user.isAccountNonLocked(), user.getAuthorities());
    }
}
