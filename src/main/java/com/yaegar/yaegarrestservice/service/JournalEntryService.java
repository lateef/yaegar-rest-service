package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.JournalEntry;
import com.yaegar.yaegarrestservice.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class JournalEntryService {
    private final JournalEntryRepository journalEntryRepository;

    public Optional<JournalEntry> findById(Long id) {
        return journalEntryRepository.findById(id);
    }

    public List<JournalEntry> findByAccount(Account account) {
        return journalEntryRepository.findByAccount(account);
    }

    public List<JournalEntry> findByAccountAndTransactionDatetimeBetween(Account account, LocalDateTime from, LocalDateTime to) {
        return journalEntryRepository.findByAccountAndTransactionDatetimeBetween(account, from, to);
    }
}
