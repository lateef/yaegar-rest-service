package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
@RequestMapping(value = "/secure-api")
public class SupplierController {
    private CompanyService companyService;
    private SupplierService supplierService;

    public SupplierController(CompanyService companyService, SupplierService supplierService) {
        this.companyService = companyService;
        this.supplierService = supplierService;
    }

    @RequestMapping(value = "/add-supplier", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Supplier>> addSupplier(@RequestBody final Supplier supplier) {
        Company company = companyService.findById(supplier.getPrincipalCompany().getId())
                .orElseThrow(NullPointerException::new);
        supplier.setPrincipalCompany(company);
        if (supplier.getSupplierCompany() != null) {
            Company supplierCompany = companyService.findById(supplier.getSupplierCompany().getId())
                    .orElse(null);
            supplier.setSupplierCompany(supplierCompany);
        }
        Supplier supplier1 = supplierService.saveSupplier(supplier);
        return ResponseEntity.ok().body(singletonMap("success", supplier1));
    }

    @RequestMapping(value = "/get-suppliers/{companyId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Supplier>>> getSuppliers(@PathVariable Long companyId) {
        List<Supplier> suppliers = supplierService.getSuppliersByPrincipalCompanyId(companyId);
        return ResponseEntity.ok().body(singletonMap("success", suppliers));
    }

    @RequestMapping(value = "/add-supplier-product", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Supplier>> addSupplierProduct(@RequestBody final Supplier supplier) {
        final Supplier supplier1 = supplierService.findById(supplier.getId()).orElseThrow(NullPointerException::new);
        final Product product = supplier.getProducts().stream().findFirst().orElseThrow(NullPointerException::new);
        supplier1.getProducts().add(product);
        final Supplier supplier2 = supplierService.saveSupplier(supplier1);
        return ResponseEntity.ok().body(singletonMap("success", supplier2));
    }
}
