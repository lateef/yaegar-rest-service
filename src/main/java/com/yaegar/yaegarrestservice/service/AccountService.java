package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.model.enums.ProductClassifier;
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

    public List<Account> findByParentId(Long parentId) {
        return accountRepository.findByParentId(parentId);
    }

    public List<Account> findByParentIdAndAccountType(Long parentId, AccountType accountType) {
        return accountRepository.findByParentIdAndAccountType(parentId, accountType);
    }

    public List<Account> findByParentIdAndProductClassifier(Long parentId, ProductClassifier productClassifier) {
        return accountRepository.findByParentIdAndProductClassifier(parentId, productClassifier);
    }

    public List<Account> findByAccountChartOfAccountsId(Long accountChartOfAccountsId) {
        return accountRepository.findByAccountChartOfAccountsId(accountChartOfAccountsId);
    }

    public Account addAccount(Account account, User createdBy) {
        return addAccount(account.getParentId(), account.getName(), account.getProductClassifier(), createdBy);
    }

    public Account addAccount(Long parentAccountId, String name, ProductClassifier productClassifier, User createdBy) {
        Account parentAccount = findById(parentAccountId)
                .orElseThrow(NullPointerException::new);
        Account account = new Account();
        account.setParentId(parentAccount.getId());
        account.setName(name.trim());
        account.setDescription(name.trim());
        account.setAccountChartOfAccountsId(parentAccount.getAccountChartOfAccountsId());
        account.setProductClassifier(productClassifier);
        account.setCreatedBy(createdBy.getId());
        account.setUpdatedBy(createdBy.getId());
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
