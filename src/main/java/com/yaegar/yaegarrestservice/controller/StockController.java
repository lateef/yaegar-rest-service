package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.AccountService;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.StockService;
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
import java.util.Objects;
import java.util.Set;

import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.PRODUCT;
import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.PRODUCT_DISCOUNT;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.EXPENSES;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.INCOME_REVENUE;
import static java.util.Collections.singletonMap;

@RestController
public class StockController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockController.class);

    private AccountService accountService;
    private CompanyService companyService;
    private StockService stockService;
    private SupplierService supplierService;

    public StockController(AccountService accountService, CompanyService companyService, StockService stockService, SupplierService supplierService) {
        this.accountService = accountService;
        this.companyService = companyService;
        this.stockService = stockService;
        this.supplierService = supplierService;
    }

    @RequestMapping(value = "/save-stock", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Stock>> saveStock(@RequestBody final Stock stock, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        Stock stock1 = null;
        if (Objects.isNull(stock.getId())) {
            stock1 = addStock(stock, user);
        }
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", stock1));
    }

    @RequestMapping(value = "/get-stocks/{companyId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<Stock>>> getStocks(@PathVariable Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        List<Stock> stocks = stockService.findByCompanyStockId(companyId);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", stocks));
    }

    @Transactional
    Stock addStock(@RequestBody Stock stock, User user) {
        Company company = companyService.findById(stock.getCompanyStockId())
                .orElseThrow(NullPointerException::new);
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

        final Account incomeRevenueStockAccount = accountService.addAccount(
                salesIncome.getId(), stock.getProduct().getName(), INCOME_REVENUE, PRODUCT, user
        );
        final Account costOfSalesGoodsStockAccount = accountService.addAccount(
                purchases.getId(), stock.getProduct().getName(), EXPENSES, PRODUCT, user
        );
        final Account incomeRevenueStockDiscountAccount = accountService.addAccount(
                salesDiscount.getId(), stock.getProduct().getName(), INCOME_REVENUE, PRODUCT_DISCOUNT, user
        );
        final Account costOfSalesGoodsStockDiscountAccount = accountService.addAccount(
                purchasesDiscount.getId(), stock.getProduct().getName(), EXPENSES, PRODUCT_DISCOUNT, user
        );

        final Set<Account> stockAccounts = new HashSet<>(
                Arrays.asList(incomeRevenueStockAccount,
                        costOfSalesGoodsStockAccount,
                        incomeRevenueStockDiscountAccount,
                        costOfSalesGoodsStockDiscountAccount)
        );
        return stockService.addStock(stock, stockAccounts, user);
    }
}
