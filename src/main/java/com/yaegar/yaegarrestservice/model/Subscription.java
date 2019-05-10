package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "subscription",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "subscription_plan_id", "subscription_start_datetime", "subscription_end_datetime"})})
public class Subscription extends AbstractEntity {
    private static final long serialVersionUID = 5787253836029321100L;

    public Subscription(User user, SubscriptionPlan subscriptionPlan, LocalDateTime subscriptionStartDatetime) {
        this.user = user;
        this.subscriptionPlan = subscriptionPlan;
        this.subscriptionStartDatetime = subscriptionStartDatetime;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "subscription_plan_id", referencedColumnName = "id")
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "subscription_start_datetime")
    private LocalDateTime subscriptionStartDatetime;

    @Column(name = "subscription_end_datetime")
    private LocalDateTime subscriptionEndDatetime;
}
