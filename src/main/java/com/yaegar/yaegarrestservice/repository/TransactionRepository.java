package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByJournalEntriesAccountId(UUID accountId);
}
