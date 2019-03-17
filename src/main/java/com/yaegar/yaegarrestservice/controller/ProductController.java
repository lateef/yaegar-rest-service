package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.ProductService;
import com.yaegar.yaegarrestservice.service.StockService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.singletonMap;

@RestController
public class ProductController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final StockService stockService;
    private final SupplierService supplierService;

    public ProductController(
            ProductService productService,
            StockService stockService,
            SupplierService supplierService
    ) {
        this.productService = productService;
        this.stockService = stockService;
        this.supplierService = supplierService;
    }

    @Transactional
    @RequestMapping(value = "/save-company-product", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Product>> saveCompanyProduct(@RequestBody final Product product, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        final Product product1 = productService.saveProduct(product, user);
        stockService.addStock(product1, product1.getCompany().getId(), ZERO, ZERO, null, 0.0, null, user);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", product1));
    }

    @RequestMapping(value = "/get-products", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Product>>> getProducts(ModelMap model, HttpServletRequest httpServletRequest) {
        final List<Product> products = productService.findAll();
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", products));
    }

    @RequestMapping(value = "/get-company-products/{companyId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Product>>> getCompanyProducts(@PathVariable Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        final List<Product> products = productService.findByCompanyId(companyId);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", products));
    }

    @RequestMapping(value = "/get-supplier-products/{supplierId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Product>>> getSupplierProducts(@PathVariable Long supplierId, ModelMap model, HttpServletRequest httpServletRequest) {
        final Set<Product> products = supplierService.getSupplierProductsById(supplierId).getProducts();
        final List<Product> products1 = productService.sortProducts(products);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", products1));
    }
}
