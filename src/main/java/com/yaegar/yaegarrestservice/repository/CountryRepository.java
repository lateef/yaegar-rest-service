package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Lateef Adeniji-Adele
 */
@Transactional(readOnly = true)
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCode(String code);
}
