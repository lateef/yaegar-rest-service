package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.ChartOfAccounts;
import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.JournalEntry;
import com.yaegar.yaegarrestservice.model.Stock;
import com.yaegar.yaegarrestservice.model.enums.AccountCategory;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.repository.AccountRepository;
import com.yaegar.yaegarrestservice.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.PRODUCT;
import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.PRODUCT_DISCOUNT;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.ASSETS;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.EQUITY;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.EXPENSES;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.INCOME_REVENUE;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.LIABILITIES;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {
    public static final List<AccountType> ROOT_ACCOUNT_TYPES = asList(ASSETS, LIABILITIES, EQUITY, INCOME_REVENUE, EXPENSES);

    private final AccountRepository accountRepository;
    private final CompanyService companyService;
    private final JournalEntryRepository journalEntryRepository;

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public Optional<Account> findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
            ChartOfAccounts chartOfAccounts, String accountName, AccountType accountType, AccountCategory accountCategory
    ) {
        return accountRepository.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
                chartOfAccounts, accountName, accountType, accountCategory);
    }

    public List<Account> findByParentId(Long parentId) {
        return accountRepository.findByParentId(parentId);
    }

    public List<Account> findByParentIdAndAccountType(Long parentId, AccountType accountType) {
        return accountRepository.findByParentIdAndAccountType(parentId, accountType);
    }

    public List<Account> findByParentIdAndAccountCategory(Long parentId, AccountCategory accountCategory) {
        return accountRepository.findByParentIdAndAccountCategory(parentId, accountCategory);
    }

    public List<Account> findByChartOfAccounts(ChartOfAccounts chartOfAccounts) {
        return accountRepository.findByChartOfAccounts(chartOfAccounts);
    }

    public Account addAccount(Account account) {
        return addAccount(account.getParentId(), account.getName(), account.getAccountCategory());
    }

    private Account addAccount(Long parentAccountId, String name, AccountCategory accountCategory) {
        Account parentAccount = findById(parentAccountId)
                .orElseThrow(NullPointerException::new);
        final AccountType accountTypeFromParentAccount = getAccountTypeFromParentAccount(parentAccount);
        Account account = new Account();
        account.setParentId(parentAccount.getId());
        account.setName(name.trim());
        account.setDescription(name.trim());
        account.setChartOfAccounts(parentAccount.getChartOfAccounts());
        account.setAccountType(accountTypeFromParentAccount);
        account.setAccountCategory(accountCategory);
        account.setEnable(true);
        final Integer maxCode = findByParentId(parentAccount.getId())
                .stream()
                .map(Account::getCode)
                .max(Integer::compareTo)
                .orElse(parentAccount.getCode());
        account.setCode(maxCode + 1);
        return accountRepository.save(account);
    }

    public List<Account> computeAccountTotal(List<Account> accounts) {
        return accounts
                .stream()
                .map(account -> {
                    final List<JournalEntry> journalEntries1 = journalEntryRepository.findByAccount(account);
                    final Account account1 = (journalEntries1.size() == 0) ? account : updateAccountTotals(journalEntries1);
                    return account1;
                })
                .collect(Collectors.toList());
    }

    Account updateAccountTotals(List<JournalEntry> journalEntries) {
        final Account account1 = journalEntries.get(0).getAccount();

        final BigDecimal journalEntriesTotal = journalEntries.stream()
                .map(JournalEntry::getAmount)
                .reduce(ZERO, BigDecimal::add);

        //TODO set other duration and think about performance issues
        account1.setYearToDateTotal(journalEntriesTotal);
        return accountRepository.save(account1);
    }

    public List<Account> getLeafAccounts(Long chartOfAccountsId) {
        return accountRepository.findByChartOfAccountsIdAndParentFalse(chartOfAccountsId);
    }

    private AccountType getAccountTypeFromParentAccount(Account parentAccount) {
        try {
            return AccountType.fromString(parentAccount.getName());
        } catch (IllegalArgumentException e) {
            return getAccountTypeFromParentAccount(accountRepository.findById(parentAccount.getParentId())
                    .orElseThrow(NullPointerException::new));
        }
    }

    Account getRootAccount(Account account) {
        if (Objects.nonNull(account.getParentId())) {
            account = accountRepository.findById(account.getParentId())
                    .orElseThrow(NullPointerException::new);
            account = getRootAccount(account);
        }
        return account;
    }

    public Set<Account> createStockAccounts(Stock stock) {
        final Company company = companyService.findById(stock.getCompanyStockId())
                .orElseThrow(NullPointerException::new);
        final List<Account> companyAccounts = findByChartOfAccounts(company.getChartOfAccounts());

        final Account salesIncome = getAccount(companyAccounts, "Sales Income");
        final Account purchases = getAccount(companyAccounts, "Purchases");
        final Account salesDiscount = getAccount(companyAccounts, "Sales Discount");
        final Account purchasesDiscount = getAccount(companyAccounts, "Purchases Discount");

        final Account incomeRevenueStockAccount = addAccount(salesIncome.getId(), stock.getProduct().getTitle(), PRODUCT);
        final Account costOfSalesGoodsStockAccount = addAccount(purchases.getId(), stock.getProduct().getTitle(), PRODUCT);
        final Account incomeRevenueStockDiscountAccount = addAccount(salesDiscount.getId(), stock.getProduct().getTitle(), PRODUCT_DISCOUNT);
        final Account costOfSalesGoodsStockDiscountAccount = addAccount(purchasesDiscount.getId(), stock.getProduct().getTitle(), PRODUCT_DISCOUNT);

        return new HashSet<>(
                asList(incomeRevenueStockAccount,
                        costOfSalesGoodsStockAccount,
                        incomeRevenueStockDiscountAccount,
                        costOfSalesGoodsStockDiscountAccount)
        );
    }

    private Account getAccount(List<Account> companyAccounts, String accountName) {
        return companyAccounts.stream()
                .filter(account -> account.getName().equals(accountName))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }
}
