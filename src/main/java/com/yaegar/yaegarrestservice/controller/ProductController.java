package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.AccountService;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.ProductService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.PRODUCT;
import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.PRODUCT_DISCOUNT;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.EXPENSES;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.INCOME_REVENUE;
import static java.util.Collections.singletonMap;

@RestController
public class ProductController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    private AccountService accountService;
    private CompanyService companyService;
    private ProductService productService;
    private SupplierService supplierService;

    public ProductController(AccountService accountService, CompanyService companyService, ProductService productService, SupplierService supplierService) {
        this.accountService = accountService;
        this.companyService = companyService;
        this.productService = productService;
        this.supplierService = supplierService;
    }

    @RequestMapping(value = "/add-product", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Product>> addProduct(@RequestBody final Product product, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        Product product1 = addProduct(product, user);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", product1));
    }

    @RequestMapping(value = "/get-products/{parentId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Product>>> getProducts(@PathVariable Long parentId, ModelMap model, HttpServletRequest httpServletRequest) {
        List<Account> productAccounts = accountService.findByParentIdAndAccountCategory(parentId, PRODUCT);
        List<Product> products = productService.findByAccountsIn(productAccounts);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", products));
    }

    @Transactional
    Product addProduct(@RequestBody Product product, User user) {
        Company company = companyService.findById(product.getCompany().getId())
                .orElseThrow(NullPointerException::new);

        product.setCompany(company);

        final List<Account> companyAccounts = accountService.findByAccountChartOfAccountsId(company.getChartOfAccounts().getId());

        final Account salesIncome = companyAccounts
                .stream()
                .filter(account -> account.getName().equals("Sales Income"))
                .findFirst()
                .orElseThrow(NullPointerException::new);

        final Account purchases = companyAccounts
                .stream()
                .filter(account -> account.getName().equals("Purchases"))
                .findFirst()
                .orElseThrow(NullPointerException::new);

        final Account salesDiscount = companyAccounts
                .stream()
                .filter(account -> account.getName().equals("Sales Discount"))
                .findFirst()
                .orElseThrow(NullPointerException::new);

        final Account purchasesDiscount = companyAccounts
                .stream()
                .filter(account -> account.getName().equals("Purchases Discount"))
                .findFirst()
                .orElseThrow(NullPointerException::new);

        final Account incomeRevenueProductAccount = accountService.addAccount(
                salesIncome.getId(), product.getName(), INCOME_REVENUE, PRODUCT, user
        );
        final Account costOfSalesGoodsProductAccount = accountService.addAccount(
                purchases.getId(), product.getName(), EXPENSES, PRODUCT, user
        );
        final Account incomeRevenueProductDiscountAccount = accountService.addAccount(
                salesDiscount.getId(), product.getName(), INCOME_REVENUE, PRODUCT_DISCOUNT, user
        );
        final Account costOfSalesGoodsProductDiscountAccount = accountService.addAccount(
                purchasesDiscount.getId(), product.getName(), EXPENSES, PRODUCT_DISCOUNT, user
        );

        final Set<Account> productAccounts = new HashSet<>(
                Arrays.asList(incomeRevenueProductAccount,
                        costOfSalesGoodsProductAccount,
                        incomeRevenueProductDiscountAccount,
                        costOfSalesGoodsProductDiscountAccount)
        );
        return productService.addProduct(product, productAccounts, user);
    }
}
