package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByAccount(Account account);

    List<JournalEntry> findByAccountAndTransactionDatetimeBetween(Account account, LocalDateTime from, LocalDateTime to);
}
