package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findOptionalByPhoneNumber(String phoneNumber);
}
