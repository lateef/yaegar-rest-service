package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
public class ProductController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @RequestMapping(value = "/save-product", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Product>> addProduct(@RequestBody final Product product, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        Product product1 = productService.saveProduct(product, user);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", product1));
    }

    @RequestMapping(value = "/get-products", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Product>>> getProducts(ModelMap model, HttpServletRequest httpServletRequest) {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", products));
    }
}
