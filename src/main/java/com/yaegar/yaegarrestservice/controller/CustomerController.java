package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Customer;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.CustomerService;
import com.yaegar.yaegarrestservice.util.AuthenticationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class CustomerController {
    private CompanyService companyService;
    private CustomerService customerService;

    public CustomerController(CompanyService companyService, CustomerService customerService) {
        this.companyService = companyService;
        this.customerService = customerService;
    }

    @RequestMapping(value = "/add-customer", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Customer>> addCustomer(@RequestBody final Customer customer, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        Company company = companyService.findById(customer.getCompany().getId())
                .orElseThrow(NullPointerException::new);
        customer.setCompany(company);
        if (customer.getCompanyCustomer() != null) {
            Company companyCustomer = companyService.findById(customer.getCompanyCustomer().getId())
                    .orElse(null);
            customer.setCompanyCustomer(companyCustomer);
        }
        customer.setCreatedBy(user.getId());
        customer.setUpdatedBy(user.getId());
        Customer customer1 = customerService.addCustomer(customer);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(Collections.singletonMap("success", customer1));
    }

    @RequestMapping(value = "/get-customers/{companyId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Customer>>> getCustomers(@PathVariable Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        List<Customer> customers = customerService.getCustomersByCompanyId(companyId);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(Collections.singletonMap("success", customers));
    }
}
