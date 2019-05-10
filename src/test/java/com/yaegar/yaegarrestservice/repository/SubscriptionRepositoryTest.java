package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Subscription;
import com.yaegar.yaegarrestservice.model.SubscriptionPlan;
import com.yaegar.yaegarrestservice.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SubscriptionRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    public void whenFindId_thenReturnSubscription() {
        //given
        String phoneNumber = "123456789";

        User expectedUser = new User();
        expectedUser.setPhoneNumber(phoneNumber);
        expectedUser.setAcceptedTerms(true);
        entityManager.persist(expectedUser);

        SubscriptionPlan expectedSubscriptionPlan = new SubscriptionPlan("Free (1 month)", 1, 1, ZERO, ZERO, "NGN");
        entityManager.persist(expectedSubscriptionPlan);

        Subscription expectedSubscription = new Subscription(expectedUser, expectedSubscriptionPlan, LocalDateTime.now());

        entityManager.persist(expectedSubscription);
        entityManager.flush();

        //when
        Subscription actualSubscription = subscriptionRepository.findById(1L).get();

        //then
        assertThat(actualSubscription, sameBeanAs(expectedSubscription));
    }
}