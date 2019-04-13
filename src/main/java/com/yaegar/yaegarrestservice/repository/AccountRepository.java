package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.ChartOfAccounts;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.model.enums.AccountCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
            ChartOfAccounts chartOfAccounts, String name, AccountType accountType, AccountCategory accountCategory
    );

    List<Account> findByParentId(Long parentId);

    List<Account> findByParentIdAndAccountType(Long parentId, AccountType accountType);

    List<Account> findByParentIdAndAccountCategory(Long parentId, AccountCategory accountCategory);

    List<Account> findByChartOfAccountsIdAndParentFalse(Long chartOfAccountsId);

    List<Account> findByChartOfAccounts(ChartOfAccounts chartOfAccounts);
}
