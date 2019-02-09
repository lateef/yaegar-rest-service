package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Country;
import com.yaegar.yaegarrestservice.model.Phone;
import com.yaegar.yaegarrestservice.model.Role;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.CountryRepository;
import com.yaegar.yaegarrestservice.repository.RoleRepository;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import com.yaegar.yaegarrestservice.util.PhoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.yaegar.yaegarrestservice.model.Role.AUTHORITY_USER;
import static java.util.Collections.singletonMap;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private CountryRepository countryRepository;
    private RoleRepository roleRepository;
    private UserRepository userRepository;

    public UserService(
            CountryRepository countryRepository, RoleRepository roleRepository, UserRepository userRepository
    ) {
        this.countryRepository = countryRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public Map<String, User> createAccount(User user) {
        if (user.getId() != null) {
            return singletonMap("There has been a problem", user);
        }
        try {
            Phone phone = getPrincipalPhone(user);
            if (!isValidNumber(phone.getNumber(), phone.getCountry().getCode())) {
                return singletonMap("Not a valid number", user);
            }

            cleanPhoneNumberAndCode(phone);
            phone.setConfirmationCode(String.format("%06d", new Random().nextInt(1000000)));
            user.setPhoneNumber(phone.getNumber());

            Optional<User> existingUser = userRepository.findOptionalByPhoneNumber(phone.getNumber());
            if (existingUser.isPresent()) {
                return singletonMap("Phone already registered", user);
            }

            Country country = countryRepository.findByCode(phone.getCountry().getCode())
            .orElseThrow(NullPointerException::new);
            phone.setCountry(country);
            user.setCountry(country);
            user.setAcceptedTerms(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);

            Set<Role> roleSet = new HashSet<>();
            Optional<Role> role = roleRepository.findByAuthority(AUTHORITY_USER);
            role.ifPresent(r -> {
                roleSet.add(r);
                user.setRoles(roleSet);
            });
            User user1 = userRepository.save(user);
            user1.eraseCredentials();
            return singletonMap("success", user1);
        } catch (NullPointerException e) {
            return singletonMap("No phone number supplied", user);
        }
    }

    public Map<String, User> logIn(User user) {
        try {
            Phone phone = getPrincipalPhone(user);
            cleanPhoneNumberAndCode(phone);

            Optional<User> existingUserOptional = userRepository.findOptionalByPhoneNumber(phone.getNumber());

            if (existingUserOptional.isPresent()) {
                User existingUser = existingUserOptional.get();
                Phone existingPrincipalPhone = getPrincipalPhone(existingUser);
                existingPrincipalPhone.setConfirmationCode(String.format("%06d", new Random().nextInt(1000000)));
                existingPrincipalPhone.setConfirmed(false);

                if (existingPrincipalPhone.equals(phone)) {
                    User user1 = userRepository.save(existingUser);
                    user1.eraseCredentials();
                    return singletonMap("success", user1);
                } else {
                    return singletonMap("This is not the phone you registered with", user);
                }
            } else {
                return singletonMap("This number has not been registered, please create a new account", user);
            }
        } catch (NullPointerException e) {
            return singletonMap("No phone number supplied", user);
        }
    }

    private void cleanPhoneNumberAndCode(Phone phone) {
        phone.setCode(phone.getCode().replaceAll("\\+", ""));
        phone.setNumber(phone.getNumber().replaceAll("\\s+", ""));
    }

    private Phone getPrincipalPhone(User user) {
        return user
                .getPhones()
                .stream()
                .filter(Phone::isPrincipal)
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    public Map<String, User> confirmAccount(User user) {
        try {
            Phone principalPhone = getPrincipalPhone(user);
            Optional<User> existingUserOptional = userRepository.findOptionalByPhoneNumber(principalPhone.getNumber().replaceAll("\\s+", ""));
            if (existingUserOptional.isPresent()) {
                User existingUser = existingUserOptional.get();
                Phone existingPrincipalPhone = getPrincipalPhone(existingUser);
                if (existingPrincipalPhone.getConfirmationCode() != null
                        && existingPrincipalPhone.getConfirmationCode().equals(principalPhone.getConfirmationCode())) {
                    existingPrincipalPhone.setConfirmed(true);
                    existingPrincipalPhone.setConfirmationCode(null);
                    userRepository.save(existingUser);
                    existingUser.eraseCredentials();
                    return singletonMap("success", existingUser);
                } else {
                    return singletonMap("This code is invalid", user);
                }
            } else {
                return singletonMap("This number has not been registered, please create a new account", user);
            }
        } catch (NullPointerException e) {
            return singletonMap("No phone number supplied", user);
        }
    }

    private boolean isValidNumber(String numberToParse, String defaultRegion) {
        return PhoneUtil.isValidNumber(numberToParse, defaultRegion);
    }
}