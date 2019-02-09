package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.JournalEntry;
import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.AccountRepository;
import com.yaegar.yaegarrestservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Transaction addTransaction(Transaction transaction, User createdBy) {
        final List<JournalEntry> journalEntries = transaction.getJournalEntries()
                .stream()
                .map(journalEntry -> {
                    Account account = accountRepository
                            .findById(journalEntry.getAccount().getId())
                            .orElseThrow(NullPointerException::new);
                    journalEntry.setAccount(account);
                    journalEntry.setCreatedBy(createdBy.getId());
                    journalEntry.setUpdatedBy(createdBy.getId());
                    return journalEntry;
                })
                .collect(Collectors.toList());
        transaction.setCreatedBy(createdBy.getId());
        transaction.setUpdatedBy(createdBy.getId());
        transaction.setJournalEntries(journalEntries);
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findByJournalEntriesAccountId(accountId);
    }
}
