package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Customer;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CustomerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

    private CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer, User user) {
        if (Objects.isNull(customer.getId())) {
            customer.setName(customer.getName().trim());
            customer.setCreatedBy(user.getId());
        }
        customer.setUpdatedBy(user.getId());
        return customerRepository.save(customer);
    }

    public List<Customer> getCustomersByPrincipalCompanyId(Long principalCompanyId) {
        return customerRepository.findByPrincipalCompanyId(principalCompanyId);
    }
}
