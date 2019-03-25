package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product saveProduct(Product product, User user) {
        //TODO check variant does not already exist
        product.setCreatedBy(user.getId());
        product.setUpdatedBy(user.getId());
        return productRepository.save(product);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findByCompanyId(Long companyId) {
        return productRepository.findByCompanyId(companyId);
    }

    public List<Product> sortProducts(Set<Product> products) {
        return products
                .stream()
                .sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.toList());
    }
}
