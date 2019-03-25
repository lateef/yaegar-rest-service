package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class CompanyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyController.class);

    private CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Transactional
    @RequestMapping(value = "/add-company", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Company>> addCompany(
            @RequestBody final Company company, ModelMap model, HttpServletRequest httpServletRequest)
            throws IOException {
        final User user = (User) model.get("user");
        Company company1 = companyService.addCompany(company, user);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(Collections.singletonMap("success", company1));
    }

    @RequestMapping(value = "/get-user-companies", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Company>>> getCompanies(ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        List<Company> companies = companyService.getCompaniesByEmployeesIn(Collections.singletonList(user));
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(Collections.singletonMap("success", companies));
    }
}
