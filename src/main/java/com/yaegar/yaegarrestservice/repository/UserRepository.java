package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Optional<User> findOptionalByPhoneNumber(String phoneNumber);
}
