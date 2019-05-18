package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Lateef Adeniji-Adele
 */
@Transactional(readOnly = true)
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByAuthority(String authority);
}
