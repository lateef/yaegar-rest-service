package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
        Company company = companyService.findById(supplier.getPrincipalCompany().getId())
                .orElseThrow(NullPointerException::new);
        supplier.setPrincipalCompany(company);
        if (supplier.getSupplierCompany() != null) {
            Company supplierCompany = companyService.findById(supplier.getSupplierCompany().getId())
                    .orElse(null);
            supplier.setSupplierCompany(supplierCompany);
        }
        supplier.setCreatedBy(user.getId());
        supplier.setUpdatedBy(user.getId());
        Supplier supplier1 = supplierService.addSupplier(supplier);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", supplier1));
    }

    @RequestMapping(value = "/get-suppliers/{companyId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Supplier>>> getSuppliers(@PathVariable Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        List<Supplier> suppliers = supplierService.getSuppliersByPrincipalCompanyId(companyId);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", suppliers));
    }
}
