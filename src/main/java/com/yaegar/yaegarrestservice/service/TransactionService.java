package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    private final PurchaseInvoiceService purchaseInvoiceService;
    private final PurchaseOrderService purchaseOrderService;
    private final SalesOrderService salesOrderService;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            AccountService accountService,
            DateTimeProvider dateTimeProvider,
            JournalEntryRepository journalEntryRepository,
            PurchaseInvoiceService purchaseInvoiceService,
            PurchaseOrderService purchaseOrderService,
            SalesOrderService salesOrderService,
            TransactionRepository transactionRepository
    ) {
        this.accountService = accountService;
        this.dateTimeProvider = dateTimeProvider;
        this.journalEntryRepository = journalEntryRepository;
        this.purchaseInvoiceService = purchaseInvoiceService;
        this.purchaseOrderService = purchaseOrderService;
        this.salesOrderService = salesOrderService;
        this.transactionRepository = transactionRepository;
    }

    public Transaction findById(Long transactionId) {
        return transactionRepository.findById(transactionId).orElse(null);
    }

    public Transaction computePurchaseOrderPaymentTransaction(PurchaseOrder purchaseOrder, PurchaseOrder savedPurchaseOrder) {
        final Transaction transaction = purchaseOrder.getTransaction();
        transaction.setTransactionTypeId(savedPurchaseOrder.getId());

        final ChartOfAccounts chartOfAccounts = savedPurchaseOrder.getSupplier().getPrincipalCompany().getChartOfAccounts();

        final Account prepaymentAccount = getAccount(chartOfAccounts, PREPAYMENT, CASH_AND_CASH_EQUIVALENTS);

        final BigDecimal totalCredit = getJournalEntriesTotalForTransactionSide(transaction.getJournalEntries(), CREDIT);

        final AtomicInteger maxEntry = getMaxEntry(transaction);

        if (!totalCredit.equals(ZERO)) {
            JournalEntry prepaymentJournalEntry = createJournalEntry(prepaymentAccount, totalCredit, DEBIT, maxEntry, "positive");

            transaction.getJournalEntries().add(prepaymentJournalEntry);
        } else {
            LOGGER.warn("Prepayment cannot be zero {}", transaction);
        }
        return transaction;
    }

    public Transaction computePurchaseInvoicesTransaction(PurchaseOrder purchaseOrder, PurchaseOrder savedPurchaseOrder) {
        final Transaction transaction = purchaseOrder.getTransaction();
        transaction.setTransactionTypeId(savedPurchaseOrder.getId());
        final ChartOfAccounts chartOfAccounts = savedPurchaseOrder.getSupplier().getPrincipalCompany().getChartOfAccounts();

        final Account tradeCreditorsAccount = getAccount(chartOfAccounts, TRADE_CREDITORS, CURRENT_LIABILITIES);
        final Account purchasesAccount = getAccount(chartOfAccounts, PURCHASES, EXPENSES);
        final AtomicInteger maxEntry = getMaxEntry(transaction);

        final List<PurchaseInvoice> invoices = purchaseInvoiceService.sortInvoicesByDate(savedPurchaseOrder.getInvoices());


//        final AccountType debitAccountType = PURCHASES;
//        final AccountType creditAccountType = PREPAYMENT;
//
//
//
//        AccountType accountTypeDebit = null;
//        if (debitAccountType.equals(PURCHASES)) {
//            accountTypeDebit = EXPENSES;
//        } else if (debitAccountType.equals(SALES_INCOME)) {
//            accountTypeDebit = INCOME_REVENUE;
//        }
//
//        AccountType accountTypeCredit = null;
//        if (creditAccountType.equals(PREPAYMENT)) {
//            accountTypeCredit = CASH_AND_CASH_EQUIVALENTS;
//        } else if (creditAccountType.equals(TRADE_DEBTORS)) {
//            accountTypeCredit = CURRENT_ASSETS;
//        }
//        final Account debitAccount = accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
//                chartOfAccounts, debitAccountType.getType(), accountTypeDebit, null
//        ).orElseThrow(NullPointerException::new);
//
//        final Account creditAccount = accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
//                chartOfAccounts, creditAccountType.getType(), accountTypeCredit, null
//        ).orElseThrow(NullPointerException::new);


        IntStream.range(0, invoices.size())
                .forEach(idx -> {
                    final List<PurchaseInvoiceLineItem> lineItems = purchaseOrderService.sortInvoiceLineItemsIntoOrderedList(
                            invoices.get(idx).getLineItems());
                    IntStream.range(0, lineItems.size())
                            .forEach(idx1 -> {
                                JournalEntry purchasesJournalEntry = createJournalEntry(
                                        purchasesAccount, lineItems.get(idx1).getSubTotal(), DEBIT, maxEntry, "positive"
                                );
                                maxEntry.getAndIncrement();
                                transaction.getJournalEntries().add(purchasesJournalEntry);
                            });
                });

        BigDecimal totalDebitTradeCreditors = getJournalEntriesTotalForAccountAndTransactionSide(transaction.getJournalEntries(), tradeCreditorsAccount, DEBIT);
        JournalEntry tradeCreditorsJournalEntry = createJournalEntry(tradeCreditorsAccount, totalDebitTradeCreditors, CREDIT, maxEntry, "negative");
        maxEntry.getAndIncrement();
        transaction.getJournalEntries().add(tradeCreditorsJournalEntry);
        return transaction;
    }

    private Account getAccount(ChartOfAccounts chartOfAccounts, AccountType tradeCreditors, AccountType currentLiabilities) {
        final Account tradeCreditorsAccount = accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
                chartOfAccounts, tradeCreditors.getType(), currentLiabilities, null
        ).orElseThrow(NullPointerException::new);
        return tradeCreditorsAccount;
    }

    public Transaction computeSalesInvoicesTransaction(
            Transaction transaction,
            List<SalesInvoice> invoices,
            ChartOfAccounts chartOfAccounts,
            AccountType debitAccountType,
            AccountType creditAccountType,
            Long transactionTypeId
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
        final Account debitAccount = accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
                chartOfAccounts, debitAccountType.getType(), accountTypeDebit, null
        ).orElseThrow(NullPointerException::new);

        final Account creditAccount = accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
                chartOfAccounts, creditAccountType.getType(), accountTypeCredit, null
        ).orElseThrow(NullPointerException::new);

        final AtomicInteger maxEntry = getMaxEntry(transaction);

        IntStream.range(0, invoices.size())
                .forEach(idx -> {
                    final List<SalesInvoiceLineItem> lineItems = salesOrderService.sortInvoiceLineItemsIntoOrderedList(invoices.get(idx).getLineItems());
                    IntStream.range(0, lineItems.size())
                            .forEach(idx1 -> {
                                JournalEntry purchasesJournalEntry = createJournalEntry(debitAccount, lineItems.get(idx1).getSubTotal(), DEBIT, maxEntry, "positive");
                                maxEntry.getAndIncrement();

                                transaction.getJournalEntries().add(purchasesJournalEntry);
                            });
                });

        BigDecimal totalDebitPrepayments = getJournalEntriesTotalForAccountAndTransactionSide(transaction.getJournalEntries(), creditAccount, DEBIT);
        JournalEntry prepaymentJournalEntry = createJournalEntry(creditAccount, totalDebitPrepayments, CREDIT, maxEntry, "negative");
        maxEntry.getAndIncrement();
        transaction.getJournalEntries().add(prepaymentJournalEntry);
        return transaction;
    }

    public Transaction computeSalesOrderPaymentTransaction(SalesOrder salesOrder, SalesOrder savedSalesOrder) {
        final Transaction transaction = salesOrder.getTransaction();
        transaction.setTransactionTypeId(savedSalesOrder.getId());

        final Account account = accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
                salesOrder.getCustomer().getPrincipalCompany().getChartOfAccounts(),
                TRADE_DEBTORS.getType(),
                CURRENT_ASSETS,
                null
        ).orElseThrow(NullPointerException::new);

        final BigDecimal totalDebit = getJournalEntriesTotalForTransactionSide(transaction.getJournalEntries(), DEBIT);

        final AtomicInteger maxEntry = getMaxEntry(transaction);

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
    public Transaction saveTransaction(Transaction transaction) {
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

        transaction.setJournalEntries(null);
        final Transaction transaction1 = transactionRepository.save(transaction);

        final List<JournalEntry> journalEntries1 = journalEntries
                .stream()
                .map(journalEntry -> {
                    journalEntry.setTransaction(transaction1);
                    String description = getDescription(journalEntry, journalEntries);
                    validateAndSetShortDescription(journalEntry, description);
                    validateAndSetDescription(journalEntry, description);
                    if (Objects.isNull(journalEntry.getTransactionDatetime())) {
                        journalEntry.setTransactionDatetime(dateTimeProvider.now());
                    }
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

    private JournalEntry createJournalEntry(Account account, BigDecimal totalCredit, TransactionSide transactionSide, AtomicInteger maxEntry, String sign) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setTransactionSide(transactionSide);
        if ("positive".equalsIgnoreCase(sign)) {
            journalEntry.setAmount(totalCredit.abs());
        } else {
            journalEntry.setAmount(totalCredit.abs().negate());
        }
        journalEntry.setAccount(account);
        journalEntry.setEntry(maxEntry.incrementAndGet());
        journalEntry.setTransactionDatetime(dateTimeProvider.now());
        return journalEntry;
    }

    private String getDescription(JournalEntry refJournalEntry, Set<JournalEntry> journalEntries) {
        final Set<String> uniqueAccountNames = journalEntries
                .stream()
                .filter(journalEntry -> !journalEntry.getAccount().getName().equals(refJournalEntry.getAccount().getName()))
                .map(journalEntry -> journalEntry.getAccount().getName())
                .collect(Collectors.toSet());
        return refJournalEntry.getAccount().getName() + " *** " + String.join(" *** ", uniqueAccountNames);
    }

    private AtomicInteger getMaxEntry(Transaction transaction) {
        return transaction.getJournalEntries()
                .stream()
                .map(JournalEntry::getEntry)
                .max(Integer::compareTo)
                .map(AtomicInteger::new)
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
