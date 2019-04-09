package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Invoice;
import com.yaegar.yaegarrestservice.model.JournalEntry;
import com.yaegar.yaegarrestservice.model.LineItem;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.SalesOrder;
import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.model.enums.TransactionSide;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.repository.JournalEntryRepository;
import com.yaegar.yaegarrestservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.yaegar.yaegarrestservice.model.enums.AccountType.*;
import static com.yaegar.yaegarrestservice.model.enums.TransactionSide.CREDIT;
import static com.yaegar.yaegarrestservice.model.enums.TransactionSide.DEBIT;
import static com.yaegar.yaegarrestservice.service.AccountService.ROOT_ACCOUNT_TYPES;
import static java.math.BigDecimal.ZERO;

@Service
public class TransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);

    private final AccountService accountService;
    private final DateTimeProvider dateTimeProvider;
    private final JournalEntryRepository journalEntryRepository;
    private final TransactionRepository transactionRepository;

    private final PurchaseOrderService purchaseOrderService;

    public TransactionService(
            AccountService accountService,
            DateTimeProvider dateTimeProvider, JournalEntryRepository journalEntryRepository,
            TransactionRepository transactionRepository,
            PurchaseOrderService purchaseOrderService
    ) {
        this.accountService = accountService;
        this.dateTimeProvider = dateTimeProvider;
        this.journalEntryRepository = journalEntryRepository;
        this.transactionRepository = transactionRepository;
        this.purchaseOrderService = purchaseOrderService;
    }

    public Transaction computePurchaseOrderPaymentInAdvanceTransaction(PurchaseOrder purchaseOrder, PurchaseOrder savedPurchaseOrder, User updatedBy) {
        final Transaction transaction = purchaseOrder.getTransaction();
        transaction.setTransactionTypeId(savedPurchaseOrder.getId());

        final Account account = accountService.findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
                purchaseOrder.getSupplier().getPrincipalCompany().getChartOfAccounts().getId(),
                PREPAYMENT.getType(),
                CASH_AND_CASH_EQUIVALENTS,
                null
        ).orElseThrow(NullPointerException::new);

        final BigDecimal totalCredit = getJournalEntriesTotalForTransactionSide(transaction.getJournalEntries(), CREDIT);

        final Integer maxEntry = getMaxEntry(transaction);

        if (!totalCredit.equals(ZERO)) {
            JournalEntry prepaymentJournalEntry = createJournalEntry(account, totalCredit, DEBIT, maxEntry, "positive");

            transaction.getJournalEntries().add(prepaymentJournalEntry);
        } else {
            LOGGER.warn("Prepayment cannot be zero {}", transaction);
        }
        return transaction;
    }

    public Transaction computeInvoicesTransaction(
            Transaction transaction,
            List<Invoice> invoices,
            Long chartOfAccountsId,
            AccountType debitAccountType,
            AccountType creditAccountType,
            Long transactionTypeId,
            User updatedBy
    ) {
        transaction.setTransactionTypeId(transactionTypeId);

        AccountType accountTypeDebit = null;
        if (debitAccountType.equals(PURCHASES)) {
            accountTypeDebit = EXPENSES;
        } else if (debitAccountType.equals(SALES_INCOME)) {
            accountTypeDebit = INCOME_REVENUE;
        }

        AccountType accountTypeCredit = null;
        if (creditAccountType.equals(PREPAYMENT)) {
            accountTypeCredit = CASH_AND_CASH_EQUIVALENTS;
        } else if (creditAccountType.equals(TRADE_DEBTORS)) {
            accountTypeCredit = CURRENT_ASSETS;
        }
        final Account debitAccount = accountService.findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
                chartOfAccountsId, debitAccountType.getType(), accountTypeDebit, null
        ).orElseThrow(NullPointerException::new);

        final Account creditAccount = accountService.findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
                chartOfAccountsId, creditAccountType.getType(), accountTypeCredit, null
        ).orElseThrow(NullPointerException::new);

        final Integer maxEntry = getMaxEntry(transaction);
        AtomicReference<Integer> entry = new AtomicReference<>(maxEntry);

        IntStream.range(0, invoices.size())
                .forEach(idx -> {
                    final List<LineItem> lineItems = purchaseOrderService.sortLineItemsIntoOrderedList(invoices.get(idx).getLineItems());
                    IntStream.range(0, lineItems.size())
                            .forEach(idx1 -> {
                                JournalEntry purchasesJournalEntry = createJournalEntry(debitAccount, lineItems.get(idx1).getSubTotal(), DEBIT, entry.get(), "positive");
                                entry.getAndSet(entry.get() + 1);

                                transaction.getJournalEntries().add(purchasesJournalEntry);
                            });
                });

        BigDecimal totalDebitPrepayments = getJournalEntriesTotalForAccountAndTransactionSide(transaction.getJournalEntries(), creditAccount, DEBIT);
        JournalEntry prepaymentJournalEntry = createJournalEntry(creditAccount, totalDebitPrepayments, CREDIT, entry.get(), "negative");
        entry.getAndSet(entry.get() + 1);
        transaction.getJournalEntries().add(prepaymentJournalEntry);
        return transaction;
    }

    public Transaction computeSalesOrderPaymentInArrearsTransaction(SalesOrder salesOrder, SalesOrder savedSalesOrder, User user) {
        final Transaction transaction = salesOrder.getTransaction();
        transaction.setTransactionTypeId(savedSalesOrder.getId());

        final Account account = accountService.findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
                salesOrder.getCustomer().getPrincipalCompany().getChartOfAccounts().getId(),
                TRADE_DEBTORS.getType(),
                CURRENT_ASSETS,
                null
        ).orElseThrow(NullPointerException::new);

        final BigDecimal totalDebit = getJournalEntriesTotalForTransactionSide(transaction.getJournalEntries(), DEBIT);

        final Integer maxEntry = getMaxEntry(transaction);

        if (!totalDebit.equals(ZERO)) {
            JournalEntry tradeCreditorsJournalEntry = createJournalEntry(account, totalDebit, CREDIT, maxEntry, "positive");

            transaction.getJournalEntries().add(tradeCreditorsJournalEntry);
        } else {
            LOGGER.warn("Prepayment cannot be zero {}", transaction);
        }
        return transaction;
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findByJournalEntriesAccountId(accountId);
    }

    @Transactional
    public Transaction saveTransaction(Transaction transaction, User createdBy) {
        final Set<JournalEntry> journalEntries = transaction.getJournalEntries()
                .stream()
                .map(journalEntry -> {
                    Account account = accountService
                            .findById(journalEntry.getAccount().getId())
                            .orElseThrow(NullPointerException::new);
                    journalEntry.setAccount(account);
                    setTransactionSide(journalEntry);
                    return journalEntry;
                })
                .collect(Collectors.toSet());

        if (Objects.isNull(transaction.getCreatedBy())) {
            transaction.setCreatedBy(createdBy.getId());
        }
        transaction.setUpdatedBy(createdBy.getId());
        transaction.setJournalEntries(null);
        final Transaction transaction1 = transactionRepository.save(transaction);

        final String creditDescription = getCreditDescription(journalEntries);
        final String debitDescription = getDebitDescription(journalEntries);
        //TODO if either is null set with the other for now
        final List<JournalEntry> journalEntries1 = journalEntries
                .stream()
                .map(journalEntry -> {
                    journalEntry.setTransactionId(transaction1.getId());
                    String description = (journalEntry.getTransactionSide().equals(CREDIT)) ? debitDescription : creditDescription;
                    validateAndSetShortDescription(journalEntry, description);
                    validateAndSetDescription(journalEntry, description);

                    if (Objects.isNull(journalEntry.getCreatedBy())) {
                        journalEntry.setCreatedBy(createdBy.getId());
                    }
                    journalEntry.setUpdatedBy(createdBy.getId());
                    return journalEntry;
                })
                .collect(Collectors.toList());
        final List<JournalEntry> journalEntries2 = journalEntryRepository.saveAll(journalEntries1);

        transaction1.setJournalEntries(new HashSet<>(journalEntries2));
        return transactionRepository.save(transaction1);
    }

    private void validateAndSetDescription(JournalEntry journalEntry, String description) {
        if (journalEntry.getDescription() == null) {
            journalEntry.setDescription(
                    description.substring(0, (description.length() < 999) ? description.length() : 999));
        } else {
            journalEntry.setDescription(journalEntry.getDescription().substring(0,
                    (journalEntry.getDescription().length() < 999)
                            ? journalEntry.getDescription().length() : 999));
        }
    }

    private void validateAndSetShortDescription(JournalEntry journalEntry, String description) {
        if (journalEntry.getShortDescription() == null) {
            journalEntry.setShortDescription(
                    description.substring(0, (description.length() < 15) ? description.length() : 15));
        } else {
            journalEntry.setShortDescription(journalEntry.getShortDescription().substring(0,
                    (journalEntry.getShortDescription().length() < 15)
                            ? journalEntry.getShortDescription().length() : 15));
        }
    }

    private void setTransactionSide(JournalEntry journalEntry) {
        final Account rootAccount = accountService.getRootAccount(journalEntry.getAccount());

        switch (rootAccount.getAccountType()) {
            case ASSETS:
                if (journalEntry.getAmount().signum() > 0) {
                    journalEntry.setTransactionSide(DEBIT);
                } else if (journalEntry.getAmount().signum() < 0) {
                    journalEntry.setTransactionSide(CREDIT);
                }
                break;
            case LIABILITIES:
                if (journalEntry.getAmount().signum() > 0) {
                    journalEntry.setTransactionSide(CREDIT);
                } else if (journalEntry.getAmount().signum() < 0) {
                    journalEntry.setTransactionSide(DEBIT);
                }
                break;
            case EQUITY:
                if (journalEntry.getAmount().signum() > 0) {
                    journalEntry.setTransactionSide(CREDIT);
                } else if (journalEntry.getAmount().signum() < 0) {
                    journalEntry.setTransactionSide(DEBIT);
                }
                break;
            case INCOME_REVENUE:
                if (journalEntry.getAmount().signum() > 0) {
                    journalEntry.setTransactionSide(CREDIT);
                } else if (journalEntry.getAmount().signum() < 0) {
                    journalEntry.setTransactionSide(DEBIT);
                }
                break;
            case EXPENSES:
                if (journalEntry.getAmount().signum() > 0) {
                    journalEntry.setTransactionSide(DEBIT);
                } else if (journalEntry.getAmount().signum() < 0) {
                    journalEntry.setTransactionSide(CREDIT);
                }
                break;
            default:
                throw new IllegalArgumentException("Account type is not one " + String.join(ROOT_ACCOUNT_TYPES.toString()));
        }
    }

    private JournalEntry createJournalEntry(Account account, BigDecimal totalCredit, TransactionSide transactionSide, Integer maxEntry, String sign) {
        JournalEntry prepaymentJournalEntry = new JournalEntry();
        prepaymentJournalEntry.setTransactionSide(transactionSide);
        if ("positive".equalsIgnoreCase(sign)) {
            prepaymentJournalEntry.setAmount(totalCredit.abs());
        } else {
            prepaymentJournalEntry.setAmount(totalCredit.abs().negate());
        }
        prepaymentJournalEntry.setAccount(account);
        prepaymentJournalEntry.setEntry(maxEntry + 1);
        prepaymentJournalEntry.setTransactionDatetime(dateTimeProvider.now());
        return prepaymentJournalEntry;
    }

    private String getDebitDescription(Set<JournalEntry> journalEntries) {
        return getDescription(journalEntries, DEBIT);
    }

    private String getCreditDescription(Set<JournalEntry> journalEntries) {
        return getDescription(journalEntries, CREDIT);
    }

    private String getDescription(Set<JournalEntry> journalEntries, TransactionSide transactionSide) {
        final Set<String> uniqueAccountNames = journalEntries
                .stream()
                .filter(journalEntry -> journalEntry.getTransactionSide().equals(transactionSide))
                .map(journalEntry -> journalEntry.getAccount().getName())
                .collect(Collectors.toSet());
        return String.join(" *** ", uniqueAccountNames);
    }

    private Integer getMaxEntry(Transaction transaction) {
        return transaction.getJournalEntries()
                .stream()
                .map(JournalEntry::getEntry)
                .max(Integer::compareTo)
                .orElseThrow(NullPointerException::new);
    }

    private BigDecimal getJournalEntriesTotalForTransactionSide(Set<JournalEntry> journalEntries, TransactionSide transactionSide) {
        return journalEntries
                .stream()
                .filter(journalEntry -> journalEntry.getTransactionSide().equals(transactionSide))
                .map(JournalEntry::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal getJournalEntriesTotalForAccountAndTransactionSide(Set<JournalEntry> journalEntries, Account account, TransactionSide transactionSide) {
        return journalEntries
                .stream()
                .filter(journalEntry -> journalEntry.getAccount().getId().equals(account.getId()))
                .filter(journalEntry -> journalEntry.getTransactionSide().equals(transactionSide))
                .map(JournalEntry::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }
}
