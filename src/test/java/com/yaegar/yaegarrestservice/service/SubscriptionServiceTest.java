package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Subscription;
import com.yaegar.yaegarrestservice.model.SubscriptionPlan;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.repository.SubscriptionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SubscriptionServiceTest {
    @MockBean
    private DateTimeProvider dateTimeProvider;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    private SubscriptionService subscriptionService;

    @Before
    public void setUp() {
        subscriptionService = new SubscriptionService(dateTimeProvider, subscriptionRepository);
    }

    @Test
    public void shouldSubscribeUserWhen1MonthPlan() {
        //given
        final LocalDateTime now = LocalDateTime.of(2019, 3, 14, 12, 28, 5);

        User user = new User();
        SubscriptionPlan subscriptionPlan = new SubscriptionPlan("Free (1 month)", 1, 1, ZERO, ZERO, "NGN");

        Subscription expectedSubscription = new Subscription(user, subscriptionPlan, now);
        expectedSubscription.setSubscriptionEndDatetime(now.plusMonths(1L));
        Subscription subscription = new Subscription(user, subscriptionPlan, now);
        subscription.setSubscriptionEndDatetime(now.plusMonths(1L));

        when(dateTimeProvider.now()).thenReturn(now);
        when(subscriptionRepository.save(subscription)).thenReturn(expectedSubscription);

        //when
        final Subscription actualSubscription = subscriptionService.subscribe(subscriptionPlan, user);

        //then
        assertThat(actualSubscription, is(sameBeanAs(expectedSubscription)));
    }
}