package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.model.enums.AccountCategory;
import com.yaegar.yaegarrestservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
        return addAccount(account.getParentId(), account.getName(), account.getAccountType(), account.getAccountCategory(), createdBy);
    }

    public Account addAccount(Long parentAccountId, String name, AccountType accountType, AccountCategory accountCategory, User createdBy) {
        Account parentAccount = findById(parentAccountId)
                .orElseThrow(NullPointerException::new);
        Account account = new Account();
        account.setParentId(parentAccount.getId());
        account.setName(name.trim());
        account.setDescription(name.trim());
        account.setAccountChartOfAccountsId(parentAccount.getAccountChartOfAccountsId());
        account.setAccountType(accountType);
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

    public List<Account> getLeafAccounts(Long chartOfAccountsId) {
        return accountRepository.findByAccountChartOfAccountsIdAndParentFalse(chartOfAccountsId);
    }
}
