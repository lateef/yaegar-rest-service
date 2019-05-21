package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.ProductService;
import com.yaegar.yaegarrestservice.service.StockService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.singletonMap;

@Slf4j
@RestController
@RequestMapping(value = "/secure-api")
public class ProductController {
    private final CompanyService companyService;
    private final ProductService productService;
    private final StockService stockService;
    private final SupplierService supplierService;

    public ProductController(CompanyService companyService,
                             ProductService productService,
                             StockService stockService,
                             SupplierService supplierService) {
        this.companyService = companyService;
        this.productService = productService;
        this.stockService = stockService;
        this.supplierService = supplierService;
    }

    @Transactional
    @RequestMapping(value = "/save-company-product", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Product>> saveCompanyProduct(@RequestBody final Product product) {
        final Product product1 = productService.saveProduct(product);

        final Company company = companyService.findById(product1.getCompanyId())
                .orElseThrow(NullPointerException::new);

        stockService.addStock(product1, product1.getCompanyId(), ZERO, ZERO, null, 0.0, company.getLocations().get(1));
        return ResponseEntity.ok().body(singletonMap("success", product1));
    }

    @RequestMapping(value = "/get-products", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Product>>> getProducts() {
        final List<Product> products = productService.findAll();
        return ResponseEntity.ok().body(singletonMap("success", products));
    }

    @RequestMapping(value = "/get-company-products/{companyId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Product>>> getCompanyProducts(@PathVariable UUID companyId) {
        final List<Product> products = productService.findByCompanyId(companyId);
        return ResponseEntity.ok().body(singletonMap("success", products));
    }

    @RequestMapping(value = "/get-supplier-products/{supplierId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Product>>> getSupplierProducts(@PathVariable UUID supplierId) {
        final Set<Product> products = supplierService.getSupplierProductsById(supplierId).getProducts();
        final List<Product> products1 = productService.sortProducts(products);
        return ResponseEntity.ok().body(singletonMap("success", products1));
    }
}
