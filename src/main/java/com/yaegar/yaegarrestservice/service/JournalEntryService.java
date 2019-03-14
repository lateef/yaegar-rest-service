package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.JournalEntry;
import com.yaegar.yaegarrestservice.repository.JournalEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JournalEntryService.class);

    private JournalEntryRepository journalEntryRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

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
