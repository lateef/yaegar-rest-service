package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Customer;
import com.yaegar.yaegarrestservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Optional<Customer> findById(UUID id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {
        if (Objects.isNull(customer.getId())) {
            customer.setName(customer.getName().trim());
        }
        return customerRepository.save(customer);
    }

    public List<Customer> getCustomersByPrincipalCompanyId(UUID principalCompanyId) {
        return customerRepository.findByPrincipalCompanyId(principalCompanyId);
    }
}
