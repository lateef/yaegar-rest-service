package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Lateef Adeniji-Adele
 */
@Transactional(readOnly = true)
public interface CountryRepository extends JpaRepository<Country, UUID> {
    Optional<Country> findByCode(String code);
}
