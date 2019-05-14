package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
}
