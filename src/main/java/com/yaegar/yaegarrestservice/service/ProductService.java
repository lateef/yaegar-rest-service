package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product addProduct(Product product, List<Account> accounts, User user) {
        product.setAccounts(accounts);
        findByNameAndCompanyId(product.getName(), product.getCompany().getId())
                .ifPresent(e -> {
                    throw new IllegalStateException("Exception:: Product already exists");
                });
        product.setCreatedBy(user.getId());
        product.setUpdatedBy(user.getId());
        return productRepository.save(product);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findByNameAndCompanyId(String name, Long companyId) {
        return productRepository.findByNameAndCompanyId(name, companyId);
    }

    public List<Product> findByAccountsIn(List<Account> accounts) {
        return productRepository.findByAccountsIn(accounts);
    }
}
