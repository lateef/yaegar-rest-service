package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.model.enums.ProductClassifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findById(Long id);

    List<Account> findByParentId(Long parentId);

    List<Account> findByParentIdAndAccountType(Long parentId, AccountType accountType);

    List<Account> findByParentIdAndProductClassifier(Long parentId, ProductClassifier productClassifier);

    List<Account> findByAccountChartOfAccountsIdAndParentFalse(Long accountChartOfAccountsId);

    List<Account> findByAccountChartOfAccountsId(Long accountChartOfAccountsId);
}
