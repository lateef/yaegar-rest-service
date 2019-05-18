package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Subscription;
import com.yaegar.yaegarrestservice.model.SubscriptionPlan;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionService {
    private final DateTimeProvider dateTimeProvider;
    private final SubscriptionRepository subscriptionRepository;

    public Subscription subscribe(SubscriptionPlan subscriptionPlan, User user) {
        final LocalDateTime startDatetime = dateTimeProvider.now();
        final Subscription subscription = new Subscription(user, subscriptionPlan, startDatetime);
        subscription.setSubscriptionEndDatetime(startDatetime.plusMonths(subscriptionPlan.getDuration()));
        return subscriptionRepository.save(subscription);
    }
}
