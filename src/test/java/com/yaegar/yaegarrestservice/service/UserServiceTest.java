package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Country;
import com.yaegar.yaegarrestservice.model.Phone;
import com.yaegar.yaegarrestservice.model.Role;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.CountryRepository;
import com.yaegar.yaegarrestservice.repository.RoleRepository;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    private RoleRepository roleRepository;

    @MockBean
    private UserRepository userRepository;

    private UserService userService;


    @Before
    public void setUp() {
        userService = new UserService(countryRepository, roleRepository, userRepository);
    }

    @Test
    public void whenGetUserWithNoPhone_thenThrowException() {
        //then
        expectedException.expect(NullPointerException.class);

        //given
        User expectedUser = new User();
        expectedUser.setPhones(emptySet());
        expectedUser.setUuid(UUID.randomUUID().toString());

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
        expectedUser.setUuid(UUID.randomUUID().toString());

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
        expectedUser.setUuid(UUID.randomUUID().toString());

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
        Country country = new Country("United Kingdom of Great Britain & Northern Ireland", "GB", "EU");
        Phone phone = new Phone("+44", phoneNumber, true, country);

        User expectedUser = new User();
        expectedUser.setPhones(singleton(phone));
        expectedUser.setUuid(UUID.randomUUID().toString());

        Map expectedAccount = singletonMap("Phone already registered", expectedUser);
        when(userRepository.findOptionalByPhoneNumber(phoneNumber)).thenReturn(Optional.of(expectedUser));

        //when
        Map<String, User> account = userService.createAccount(expectedUser);

        //then
        assertThat(account, is(sameBeanAs(expectedAccount)));
    }

    @Test
    public void whenGetUserWithExistingPhoneFormattedWithSpaces_thenReturnValidMessage() {
        //given
        Country country = new Country("United Kingdom of Great Britain & Northern Ireland", "GB", "EU");
        Phone phone = new Phone("+44", "+44 7780 708394", true, country);
        Phone savedPhone = new Phone("44", "+447780708394", true, country);

        String uuid = UUID.randomUUID().toString();
        User user = new User();
        user.setPhones(singleton(phone));
        user.setUuid(uuid);

        User expectedUser = new User();
        expectedUser.setPhones(singleton(savedPhone));
        expectedUser.setUuid(uuid);
        expectedUser.setPhoneNumber(phone.getNumber());

        Map expectedAccount = singletonMap("Phone already registered", expectedUser);
        when(userRepository.findOptionalByPhoneNumber(savedPhone.getNumber())).thenReturn(Optional.of(expectedUser));

        //when
        Map<String, User> account = userService.createAccount(user);

        //then
        assertThat(account, is(sameBeanAs(expectedAccount)));
    }

    @Test
    public void whenGetUserWithNewPhone_thenReturnSavedUser() {
        //given
        String phoneNumber = "+447780708394";
        Country country = new Country("United Kingdom of Great Britain & Northern Ireland", "GB", "EU");
        Phone phone = new Phone("+44", phoneNumber, true, country);

        User expectedUser = new User();
        expectedUser.setPhones(singleton(phone));
        expectedUser.setUuid(UUID.randomUUID().toString());

        Map expectedAccount = singletonMap("success", expectedUser);
        when(userRepository.findOptionalByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());
        when(countryRepository.findByCode(country.getCode())).thenReturn(Optional.of(country));
        when(roleRepository.findByAuthority(AUTHORITY_USER)).thenReturn(Optional.of(new Role()));
        when(userRepository.save(expectedUser)).thenReturn(expectedUser);

        //when
        Map<String, User> account = userService.createAccount(expectedUser);

        //then
        assertThat(account, is(sameBeanAs(expectedAccount)));
    }
}