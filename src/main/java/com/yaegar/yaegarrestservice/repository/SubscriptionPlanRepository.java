package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
}
