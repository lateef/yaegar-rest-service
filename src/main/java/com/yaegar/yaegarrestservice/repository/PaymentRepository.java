package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository  extends JpaRepository<Payment, Long> {
}
