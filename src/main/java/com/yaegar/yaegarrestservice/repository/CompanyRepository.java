package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUuid(String uuid);
    Set<Company> findByEmployeesIn(Set<User> employees);
}
