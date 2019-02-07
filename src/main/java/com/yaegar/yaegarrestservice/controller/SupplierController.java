package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import com.yaegar.yaegarrestservice.util.AuthenticationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
public class SupplierController {
    private CompanyService companyService;
    private SupplierService supplierService;

    public SupplierController(CompanyService companyService, SupplierService supplierService) {
        this.companyService = companyService;
        this.supplierService = supplierService;
    }

    @RequestMapping(value = "/add-supplier", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Supplier>> addSupplier(@RequestBody final Supplier supplier, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);
        Company company = companyService.findById(supplier.getCompany().getId())
                .orElseThrow(NullPointerException::new);
        supplier.setCompany(company);
        if (supplier.getCompanySupplier() != null) {
            Company suppliedFromCompany = companyService.findById(supplier.getCompanySupplier().getId())
                    .orElse(null);
            supplier.setCompanySupplier(suppliedFromCompany);
        }
        supplier.setCreatedBy(user.getId());
        supplier.setUpdatedBy(user.getId());
        Supplier supplier1 = supplierService.addSupplier(supplier);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", supplier1));
    }

    @RequestMapping(value = "/get-suppliers/{companyId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Supplier>>> getSuppliers(@PathVariable Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);
        List<Supplier> suppliers = supplierService.getSuppliersByCompanyId(companyId);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", suppliers));
    }
}
