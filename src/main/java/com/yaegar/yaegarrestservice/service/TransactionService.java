package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState;
import com.yaegar.yaegarrestservice.model.enums.TransactionSide;
import com.yaegar.yaegarrestservice.repository.AccountRepository;
import com.yaegar.yaegarrestservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.PREPAYMENT;
import static com.yaegar.yaegarrestservice.model.enums.TransactionSide.CREDIT;
import static com.yaegar.yaegarrestservice.model.enums.TransactionSide.DEBIT;
import static java.math.BigDecimal.ZERO;

@Service
public class TransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Transaction computePurchaseOrderTransaction(
            Transaction transaction,
            Long chartOfAccountsId,
            PurchaseOrderState purchaseOrderState,
            Long transactionTypeId,
            User updatedBy
    ) {
        transaction.setTransactionTypeId(transactionTypeId);

        if (purchaseOrderState.equals(PREPAYMENT)) {
            final Account account = accountRepository.findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
                    chartOfAccountsId, PREPAYMENT.name(), null, null
            ).orElseThrow(NullPointerException::new);

            final BigDecimal totalPrepayment = transaction.getJournalEntries()
                    .stream()
                    .filter(journalEntry -> journalEntry.getTransactionSide().equals(CREDIT))
                    .map(JournalEntry::getAmount)
                    .reduce(ZERO, BigDecimal::add);

            final Integer maxEntry = transaction.getJournalEntries()
                    .stream()
                    .map(JournalEntry::getEntry)
                    .max(Integer::compareTo)
                    .orElseThrow(NullPointerException::new);

            if (!totalPrepayment.equals(ZERO)) {
                JournalEntry prepaymentJournalEntry = new JournalEntry();
                prepaymentJournalEntry.setTransactionSide(DEBIT);
                prepaymentJournalEntry.setAmount(totalPrepayment);
                prepaymentJournalEntry.setAccount(account);
                prepaymentJournalEntry.setEntry(maxEntry + 1);
                prepaymentJournalEntry.setTransactionDatetime(LocalDateTime.now());
                prepaymentJournalEntry.setCreatedBy(updatedBy.getId());
                prepaymentJournalEntry.setUpdatedBy(updatedBy.getId());

                transaction.getJournalEntries().add(prepaymentJournalEntry);
            } else {
                LOGGER.warn("Prepayment cannot be zero {}", transaction);
            }
        }
        return transaction;
    }

    public Transaction saveTransaction(Transaction transaction, User createdBy) {
        final String creditDescription = getCreditDescription(transaction);
        final String debitDescription = getDebitDescription(transaction);
        final Set<JournalEntry> journalEntries = transaction.getJournalEntries()
                .stream()
                .map(journalEntry -> {
                    Account account = accountRepository
                            .findById(journalEntry.getAccount().getId())
                            .orElseThrow(NullPointerException::new);
                    journalEntry.setAccount(account);
                    String description = (journalEntry.getTransactionSide().equals(CREDIT)) ? debitDescription : creditDescription;

                    if (journalEntry.getShortDescription() == null) {
                        journalEntry.setShortDescription(
                                description.substring(0, (description.length() < 15) ? description.length() : 15));
                    } else {
                        journalEntry.setShortDescription(journalEntry.getShortDescription().substring(0,
                                (journalEntry.getShortDescription().length() < 15)
                                        ? journalEntry.getShortDescription().length() : 15));
                    }

                    if (journalEntry.getDescription() == null) {
                        journalEntry.setDescription(
                                description.substring(0, (description.length() < 999) ? description.length() : 999));
                    } else {
                        journalEntry.setDescription(journalEntry.getDescription().substring(0,
                                (journalEntry.getDescription().length() < 999)
                                        ? journalEntry.getDescription().length() : 999));
                    }

                    journalEntry.setCreatedBy(createdBy.getId());
                    journalEntry.setUpdatedBy(createdBy.getId());
                    return journalEntry;
                })
                .collect(Collectors.toSet());
        transaction.setCreatedBy(createdBy.getId());
        transaction.setUpdatedBy(createdBy.getId());
        transaction.setJournalEntries(journalEntries);
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findByJournalEntriesAccountId(accountId);
    }

    private String getDebitDescription(Transaction transaction) {
        return getDescription(transaction, DEBIT);
    }

    private String getCreditDescription(Transaction transaction) {
        return getDescription(transaction, CREDIT);
    }

    private String getDescription(Transaction transaction, TransactionSide transactionSide) {
        final Set<String> uniqueAccountNames = transaction.getJournalEntries()
                .stream()
                .filter(journalEntry -> journalEntry.getTransactionSide().equals(transactionSide))
                .map(journalEntry -> journalEntry.getAccount().getName())
                .collect(Collectors.toSet());
        return String.join(" *** ", uniqueAccountNames);
    }
}
