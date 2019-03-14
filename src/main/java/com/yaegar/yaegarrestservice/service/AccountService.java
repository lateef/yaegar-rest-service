package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.JournalEntry;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.model.enums.AccountCategory;
import com.yaegar.yaegarrestservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;

@Service
public class AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public Optional<Account> findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
            Long id, String name, AccountType accountType, AccountCategory accountCategory
    ) {
        return accountRepository.findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
                id, name, accountType, accountCategory);
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

    public List<Account> findByAccountChartOfAccountsId(Long accountChartOfAccountsId) {
        return accountRepository.findByAccountChartOfAccountsId(accountChartOfAccountsId);
    }

    public Account addAccount(Account account, User createdBy) {
        return addAccount(account.getParentId(), account.getName(), account.getAccountCategory(), createdBy);
    }

    public Account addAccount(Long parentAccountId, String name, AccountCategory accountCategory, User createdBy) {
        Account parentAccount = findById(parentAccountId)
                .orElseThrow(NullPointerException::new);
        final AccountType accountTypeFromParentAccount = getAccountTypeFromParentAccount(parentAccount);
        Account account = new Account();
        account.setParentId(parentAccount.getId());
        account.setName(name.trim());
        account.setDescription(name.trim());
        account.setAccountChartOfAccountsId(parentAccount.getAccountChartOfAccountsId());
        account.setAccountType(accountTypeFromParentAccount);
        account.setAccountCategory(accountCategory);
        account.setCreatedBy(createdBy.getId());
        account.setUpdatedBy(createdBy.getId());
        account.setEnable(true);
        final Integer maxCode = findByParentId(parentAccount.getId())
                .stream()
                .map(Account::getCode)
                .max(Integer::compareTo)
                .orElse(parentAccount.getCode());
        account.setCode(maxCode + 1);
        return accountRepository.save(account);
    }

    public void updateAccountTotals(Account account, List<JournalEntry> journalEntries) {
        final Account account1 = accountRepository.findById(account.getId())
                .orElseThrow(NullPointerException::new);

        final BigDecimal journalEntriesTotal = journalEntries.stream()
                .map(JournalEntry::getAmount)
                .reduce(ZERO, BigDecimal::add);

        //TODO set other duration and think about performance issues
        account1.setYearToDateTotal(journalEntriesTotal);
        accountRepository.save(account1);
    }

    public List<Account> getLeafAccounts(Long chartOfAccountsId) {
        return accountRepository.findByAccountChartOfAccountsIdAndParentFalse(chartOfAccountsId);
    }

    private AccountType getAccountTypeFromParentAccount(Account parentAccount) {
        try {
            return AccountType.fromString(parentAccount.getName());
        } catch (IllegalArgumentException e) {
            return getAccountTypeFromParentAccount(accountRepository.findById(parentAccount.getParentId())
                    .orElseThrow(NullPointerException::new));
        }
    }
}
