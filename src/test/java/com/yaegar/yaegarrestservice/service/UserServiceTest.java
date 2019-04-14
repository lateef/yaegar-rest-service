package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Country;
import com.yaegar.yaegarrestservice.model.Phone;
import com.yaegar.yaegarrestservice.model.Role;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.repository.CountryRepository;
import com.yaegar.yaegarrestservice.repository.RoleRepository;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import com.yaegar.yaegarrestservice.resource.PhoneValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static com.yaegar.yaegarrestservice.model.Role.AUTHORITY_USER;
import static java.util.Collections.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UserServiceTest {
    public ExpectedException expectedException = ExpectedException.none();

    @MockBean
    private CountryRepository countryRepository;

    @MockBean
    private DateTimeProvider dateTimeProvider;

    @MockBean
    private PhoneValidator phoneValidator;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private UserRepository userRepository;

    private UserService userService;

    private String countryCode = "GB";

    @Before
    public void setUp() {
        userService = new UserService(countryRepository, dateTimeProvider, phoneValidator, roleRepository, userRepository);
    }

    @Test
    public void whenGetUserWithNoPhone_thenThrowException() {
        //then
        expectedException.expect(NullPointerException.class);

        //given
        User expectedUser = new User();
        expectedUser.setPhones(emptySet());

        //when
        Map<String, User> account = userService.createAccount(expectedUser);

        //then
        Map expectedAccount = singletonMap("No phone number supplied", expectedUser);
        assertThat(account, is(sameBeanAs(expectedAccount)));
    }

    @Test
    public void whenGetUserWithNullPhoneNumber_thenThrowException() {
        //then
        expectedException.expect(NullPointerException.class);

        //given
        Country country = new Country("United Kingdom of Great Britain & Northern Ireland", "GB", "EU");
        Phone phone = new Phone(null, null, true, country);

        User expectedUser = new User();
        expectedUser.setPhones(singleton(phone));

        //when
        Map<String, User> account = userService.createAccount(expectedUser);

        //then
        Map expectedAccount = singletonMap("Not a valid number", expectedUser);
        assertThat(account, is(sameBeanAs(expectedAccount)));
    }

    @Test
    public void whenGetUserWithBadPhone_thenReturnValidMessage() {
        //given
        Country country = new Country("United Kingdom of Great Britain & Northern Ireland", "GB", "EU");
        Phone phone = new Phone("+44", "80708394", true, country);

        User expectedUser = new User();
        expectedUser.setPhones(singleton(phone));

        Map expectedAccount = singletonMap("Not a valid number", expectedUser);

        //when
        Map<String, User> account = userService.createAccount(expectedUser);

        //then
        assertThat(account, is(sameBeanAs(expectedAccount)));
    }

    @Test
    public void whenGetUserWithExistingPhone_thenReturnValidMessage() {
        //given
        String phoneNumber = "+447780708394";
        Country country = new Country("United Kingdom of Great Britain & Northern Ireland", countryCode, "EU");
        Phone phone = new Phone("+44", phoneNumber, true, country);

        User expectedUser = new User();
        expectedUser.setPhones(singleton(phone));

        Map expectedAccount = singletonMap("Phone already registered", expectedUser);
        when(userRepository.findOptionalByPhoneNumber(phoneNumber)).thenReturn(Optional.of(expectedUser));
        when(phoneValidator.isValidNumber(phoneNumber, countryCode)).thenReturn(true);

        //when
        Map<String, User> account = userService.createAccount(expectedUser);

        //then
        assertThat(account, is(sameBeanAs(expectedAccount)));
    }

    @Test
    public void whenGetUserWithExistingPhoneFormattedWithSpaces_thenReturnValidMessage() {
        //given
        final String phoneNumber = "+44 7780 708394";
        Country country = new Country("United Kingdom of Great Britain & Northern Ireland", "GB", "EU");
        Phone phone = new Phone("+44", phoneNumber, true, country);
        Phone savedPhone = new Phone("44", "+447780708394", true, country);

        User user = new User();
        user.setPhones(singleton(phone));

        User expectedUser = new User();
        expectedUser.setPhones(singleton(savedPhone));
        expectedUser.setPhoneNumber(savedPhone.getNumber());

        Map expectedAccount = singletonMap("Phone already registered", expectedUser);
        when(userRepository.findOptionalByPhoneNumber(savedPhone.getNumber())).thenReturn(Optional.of(expectedUser));
        when(phoneValidator.isValidNumber(phoneNumber, countryCode)).thenReturn(true);

        //when
        Map<String, User> account = userService.createAccount(user);

        //then
        assertThat(account.get("Phone already registered"),
                sameBeanAs(expectedAccount.get("Phone already registered")).ignoring("phones.confirmationCode"));
    }

    @Test
    public void whenGetUserWithNewPhone_thenReturnSavedUser() {
        //given
        String phoneNumber = "+447780708394";
        Country country = new Country("United Kingdom of Great Britain & Northern Ireland", "GB", "EU");
        Phone phone = new Phone("+44", phoneNumber, true, country);

        User expectedUser = new User();
        expectedUser.setPhones(singleton(phone));

        Map expectedAccount = singletonMap("success", expectedUser);
        when(userRepository.findOptionalByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());
        when(phoneValidator.isValidNumber(phoneNumber, countryCode)).thenReturn(true);
        when(countryRepository.findByCode(country.getCode())).thenReturn(Optional.of(country));
        when(roleRepository.findByAuthority(AUTHORITY_USER)).thenReturn(Optional.of(new Role()));
        when(userRepository.save(expectedUser)).thenReturn(expectedUser);

        //when
        Map<String, User> account = userService.createAccount(expectedUser);

        //then
        assertThat(account, is(sameBeanAs(expectedAccount)));
    }
}