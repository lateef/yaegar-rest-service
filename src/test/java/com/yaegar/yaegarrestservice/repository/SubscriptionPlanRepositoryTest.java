package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.SubscriptionPlan;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SubscriptionPlanRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Test
    public void whenFindId_thenReturnSubscriptionPlan() {
        //given
        SubscriptionPlan expectedSubscriptionPlan = new SubscriptionPlan("Free (1 month)", 1, 1, ZERO, ZERO, "NGN");
        entityManager.persist(expectedSubscriptionPlan);
        entityManager.flush();

        //when
        SubscriptionPlan actualSubscriptionPlan = subscriptionPlanRepository.findById(expectedSubscriptionPlan.getId()).get();

        //then
        assertThat(actualSubscriptionPlan, sameBeanAs(expectedSubscriptionPlan));
    }
}