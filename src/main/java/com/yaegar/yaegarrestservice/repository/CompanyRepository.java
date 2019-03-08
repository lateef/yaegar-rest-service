package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByEmployeesIn(List<User> employees);
}
