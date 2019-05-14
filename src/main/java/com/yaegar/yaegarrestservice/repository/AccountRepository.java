package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.ChartOfAccounts;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.model.enums.AccountCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
            ChartOfAccounts chartOfAccounts, String name, AccountType accountType, AccountCategory accountCategory
    );

    List<Account> findByParentId(UUID parentId);

    List<Account> findByParentIdAndAccountType(UUID parentId, AccountType accountType);

    List<Account> findByParentIdAndAccountCategory(UUID parentId, AccountCategory accountCategory);

    List<Account> findByChartOfAccountsIdAndParentFalse(UUID chartOfAccountsId);

    List<Account> findByChartOfAccounts(ChartOfAccounts chartOfAccounts);
}
