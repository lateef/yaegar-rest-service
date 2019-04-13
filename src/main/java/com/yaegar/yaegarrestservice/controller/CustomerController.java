package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Customer;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/secure-api")
public class CustomerController {
    private CompanyService companyService;
    private CustomerService customerService;

    public CustomerController(CompanyService companyService, CustomerService customerService) {
        this.companyService = companyService;
        this.customerService = customerService;
    }

    @RequestMapping(value = "/add-customer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Customer>> addCustomer(@RequestBody final Customer customer) {
        Company company = companyService.findById(customer.getPrincipalCompany().getId())
                .orElseThrow(NullPointerException::new);
        customer.setPrincipalCompany(company);
        if (customer.getCustomerCompany() != null) {
            Company customerCompany = companyService.findById(customer.getCustomerCompany().getId())
                    .orElse(null);
            customer.setCustomerCompany(customerCompany);
        }
        Customer customer1 = customerService.saveCustomer(customer);
        return ResponseEntity.ok().body(Collections.singletonMap("success", customer1));
    }

    @RequestMapping(value = "/get-customers/{companyId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Customer>>> getCustomers(@PathVariable Long companyId) {
        List<Customer> customers = customerService.getCustomersByPrincipalCompanyId(companyId);
        return ResponseEntity.ok().body(Collections.singletonMap("success", customers));
    }
}
