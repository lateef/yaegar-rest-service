package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.User;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenFindOptionalByPhoneNumber_andNotAcceptedTerms_thenReturnUser() {
        //then
        expectedException.expect(ConstraintViolationException.class);

        //given
        String phoneNumber = "123456789";

        User expectedUser = new User();
        expectedUser.setPhoneNumber(phoneNumber);
        expectedUser.setUuid(UUID.randomUUID().toString());
        entityManager.persist(expectedUser);
        entityManager.flush();

        //when
        userRepository.findOptionalByPhoneNumber(phoneNumber);
    }

    @Test
    public void whenFindOptionalByPhoneNumber_andNotUuid_thenReturnUser() {
        //then
        expectedException.expect(ConstraintViolationException.class);

        //given
        String phoneNumber = "123456789";

        User expectedUser = new User();
        expectedUser.setPhoneNumber(phoneNumber);
        expectedUser.setAcceptedTerms(true);
        entityManager.persist(expectedUser);
        entityManager.flush();

        //when
        userRepository.findOptionalByPhoneNumber(phoneNumber);
    }

    @Test
    public void whenFindOptionalByPhoneNumber_thenReturnUser() {
        //given
        String phoneNumber = "123456789";

        User expectedUser = new User();
        expectedUser.setPhoneNumber(phoneNumber);
        expectedUser.setAcceptedTerms(true);
        expectedUser.setUuid(UUID.randomUUID().toString());
        entityManager.persist(expectedUser);
        entityManager.flush();

        //when
        Optional<User> actualUser = userRepository.findOptionalByPhoneNumber(phoneNumber);

        //then
        assertThat(actualUser.get().getPhoneNumber(), is(expectedUser.getPhoneNumber()));
    }
}