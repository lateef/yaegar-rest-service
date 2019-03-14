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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.yaegar.yaegarrestservice.model.enums.AccountType.PREPAYMENT;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.TRADE_DEBTORS;
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
                PREPAYMENT.name(),
                null,
                null
        ).orElseThrow(NullPointerException::new);

        final BigDecimal totalCredit = getJournalEntriesTotalForTransactionSide(transaction.getJournalEntries(), CREDIT);

        final Integer maxEntry = getMaxEntry(transaction);

        if (!totalCredit.equals(ZERO)) {
            JournalEntry prepaymentJournalEntry = createJournalEntry(account, totalCredit, DEBIT, maxEntry);

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

        final Account debitAccount = accountService.findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
                chartOfAccountsId, debitAccountType.name(), null, null
        ).orElseThrow(NullPointerException::new);

        final Account creditAccount = accountService.findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
                chartOfAccountsId, creditAccountType.name(), null, null
        ).orElseThrow(NullPointerException::new);

        final Integer maxEntry = getMaxEntry(transaction);
        AtomicReference<Integer> entry = new AtomicReference<>(maxEntry);

        IntStream.range(0, invoices.size())
                .forEach(idx -> {
                    final List<LineItem> lineItems = purchaseOrderService.sortLineItemsIntoOrderedList(invoices.get(idx).getLineItems());
                    IntStream.range(0, lineItems.size())
                            .forEach(idx1 -> {
                                JournalEntry purchasesJournalEntry = createJournalEntry(debitAccount, lineItems.get(idx1).getSubTotal(), DEBIT, entry.get());
                                entry.getAndSet(entry.get() + 1);

                                transaction.getJournalEntries().add(purchasesJournalEntry);
                            });
                });

        BigDecimal totalDebitPrepayments = getJournalEntriesTotalForAccountAndTransactionSide(transaction.getJournalEntries(), creditAccount, DEBIT);
        JournalEntry prepaymentJournalEntry = createJournalEntry(creditAccount, totalDebitPrepayments, CREDIT, entry.get());
        entry.getAndSet(entry.get() + 1);
        transaction.getJournalEntries().add(prepaymentJournalEntry);
        return transaction;
    }

    public Transaction computeSalesOrderPaymentInArrearsTransaction(SalesOrder salesOrder, SalesOrder savedSalesOrder, User user) {
        final Transaction transaction = salesOrder.getTransaction();
        transaction.setTransactionTypeId(savedSalesOrder.getId());

        final Account account = accountService.findByAccountChartOfAccountsIdAndNameAndAccountTypeAndAccountCategory(
                salesOrder.getCustomer().getPrincipalCompany().getChartOfAccounts().getId(),
                TRADE_DEBTORS.name(),
                null,
                null
        ).orElseThrow(NullPointerException::new);

        final BigDecimal totalDebit = getJournalEntriesTotalForTransactionSide(transaction.getJournalEntries(), DEBIT);

        final Integer maxEntry = getMaxEntry(transaction);

        if (!totalDebit.equals(ZERO)) {
            JournalEntry tradeCreditorsJournalEntry = createJournalEntry(account, totalDebit, CREDIT, maxEntry);

            transaction.getJournalEntries().add(tradeCreditorsJournalEntry);
        } else {
            LOGGER.warn("Prepayment cannot be zero {}", transaction);
        }
        return transaction;
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findByJournalEntriesAccountId(accountId);
    }

    public Transaction saveTransaction(Transaction transaction, User createdBy) {
        final String creditDescription = getCreditDescription(transaction);
        final String debitDescription = getDebitDescription(transaction);
        final Set<JournalEntry> journalEntries = transaction.getJournalEntries()
                .stream()
                .map(journalEntry -> {
                    Account account = accountService
                            .findById(journalEntry.getAccount().getId())
                            .orElseThrow(NullPointerException::new);
                    journalEntry.setAccount(account);
                    setTransactionSide(journalEntry);

                    String description = (journalEntry.getTransactionSide().equals(CREDIT)) ? debitDescription : creditDescription;
                    validateAndSetShortDescription(journalEntry, description);
                    validateAndSetDescription(journalEntry, description);

                    if (Objects.isNull(journalEntry.getCreatedBy())) {
                        journalEntry.setCreatedBy(createdBy.getId());
                    }
                    journalEntry.setUpdatedBy(createdBy.getId());
                    return journalEntry;
                })
                .collect(Collectors.toSet());
        if (Objects.isNull(transaction.getCreatedBy())) {
            transaction.setCreatedBy(createdBy.getId());
        }
        transaction.setUpdatedBy(createdBy.getId());
        transaction.setJournalEntries(journalEntries);
        return transactionRepository.save(transaction);
    }

    public Set<Account> computeAccountTotal(Transaction transaction) {
        return transaction.getJournalEntries()
                .stream()
                .map(JournalEntry::getAccount)
                .collect(Collectors.toSet())
                .stream()
                .map(account -> {
                    final Account account1 = accountService.findById(account.getId())
                            .orElseThrow(NullPointerException::new);

                    final List<JournalEntry> journalEntries = journalEntryRepository.findByAccount(account1);
                    return accountService.updateAccountTotals(account, journalEntries);
                })
                .collect(Collectors.toSet());
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
        final AccountType rootAccountTypeFromAccount = accountService.getRootAccountTypeFromAccount(journalEntry.getAccount());

        switch (rootAccountTypeFromAccount) {
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

    private JournalEntry createJournalEntry(Account account, BigDecimal totalCredit, TransactionSide transactionSide, Integer maxEntry) {
        JournalEntry prepaymentJournalEntry = new JournalEntry();
        prepaymentJournalEntry.setTransactionSide(transactionSide);
        prepaymentJournalEntry.setAmount(totalCredit);
        prepaymentJournalEntry.setAccount(account);
        prepaymentJournalEntry.setEntry(maxEntry + 1);
        prepaymentJournalEntry.setTransactionDatetime(dateTimeProvider.now());
        return prepaymentJournalEntry;
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
                .filter(journalEntry -> journalEntry.getAccount().equals(account))
                .filter(journalEntry -> journalEntry.getTransactionSide().equals(transactionSide))
                .map(JournalEntry::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }
}
