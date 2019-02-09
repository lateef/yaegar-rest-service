package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByJournalEntriesAccountId(Long accountId);
}
