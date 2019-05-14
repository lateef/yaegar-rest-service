package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.model.enums.AccountCategory;
import com.yaegar.yaegarrestservice.model.enums.AccountType;
import com.yaegar.yaegarrestservice.model.enums.TransactionSide;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.repository.JournalEntryRepository;
import com.yaegar.yaegarrestservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.yaegar.yaegarrestservice.model.enums.AccountType.*;
import static com.yaegar.yaegarrestservice.model.enums.TransactionSide.CREDIT;
import static com.yaegar.yaegarrestservice.model.enums.TransactionSide.DEBIT;
import static com.yaegar.yaegarrestservice.model.enums.TransactionType.PURCHASE_ORDER;
import static com.yaegar.yaegarrestservice.model.enums.TransactionType.SALES_ORDER;
import static com.yaegar.yaegarrestservice.service.AccountService.ROOT_ACCOUNT_TYPES;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountService accountService;
    private final DateTimeProvider dateTimeProvider;
    private final JournalEntryRepository journalEntryRepository;
    private final TransactionRepository transactionRepository;

    public Transaction findById(UUID transactionId) {
        return transactionRepository.findById(transactionId).orElse(null);
    }

    public Transaction computePurchaseOrderPaymentTransaction(PurchaseOrder purchaseOrder, PurchaseOrder savedPurchaseOrder) {
        final Transaction savedTransaction = savedPurchaseOrder.getTransaction();
        final ChartOfAccounts chartOfAccounts = savedPurchaseOrder.getSupplier().getPrincipalCompany().getChartOfAccounts();
        final Transaction transaction = purchaseOrder.getTransaction();
        transaction.setTransactionTypeId(savedPurchaseOrder.getId());
        final Account tradeCreditorsAccount = getAccount(chartOfAccounts, TRADE_CREDITORS.getType(), CURRENT_LIABILITIES);
        final Account prepaymentAccount = getAccount(chartOfAccounts, PREPAYMENT.getType(), CASH_AND_CASH_EQUIVALENTS);
        final AtomicInteger maxEntry = getMaxEntry(transaction);

        final List<JournalEntry> unsavedJournalEntries = filterUnsavedJournalEntries(transaction);
        BigDecimal prepayment = getJournalEntriesTotalForTransactionSide(new HashSet<>(unsavedJournalEntries), CREDIT);

        final BigDecimal tradeCreditors = (savedTransaction != null) ?
                getJournalEntriesTotalForAccount(savedTransaction.getJournalEntries(), tradeCreditorsAccount) : ZERO;

        if (tradeCreditors.compareTo(ZERO) > 0) {
            BigDecimal paymentForGoods;
            if (tradeCreditors.compareTo(prepayment) < 0) {
                paymentForGoods = tradeCreditors;
                prepayment = prepayment.subtract(tradeCreditors);

                JournalEntry prepaymentJournalEntry = createJournalEntry(prepaymentAccount, prepayment, DEBIT, maxEntry, "positive");
                transaction.getJournalEntries().add(prepaymentJournalEntry);
            } else if (tradeCreditors.compareTo(prepayment) > 0) {
                paymentForGoods = prepayment;
            } else {
                paymentForGoods = tradeCreditors;
            }

            JournalEntry tradeCreditorsJournalEntry = createJournalEntry(tradeCreditorsAccount, paymentForGoods, DEBIT, maxEntry, "negative");
            transaction.getJournalEntries().add(tradeCreditorsJournalEntry);

        } else {
            JournalEntry prepaymentJournalEntry = createJournalEntry(prepaymentAccount, prepayment, DEBIT, maxEntry, "positive");
            transaction.getJournalEntries().add(prepaymentJournalEntry);
        }

        return saveTransaction(transaction);
    }

    public Transaction computePurchaseInvoicesTransaction(PurchaseOrder purchaseOrder, PurchaseOrder savedPurchaseOrder) {
        final Transaction savedTransaction = savedPurchaseOrder.getTransaction();
        final ChartOfAccounts chartOfAccounts = savedPurchaseOrder.getSupplier().getPrincipalCompany().getChartOfAccounts();
        final Transaction transaction;
        if (purchaseOrder.getTransaction() == null) {
            transaction = new Transaction();
            transaction.setTransactionType(PURCHASE_ORDER);
            transaction.setTransactionTypeId(purchaseOrder.getId());
            transaction.setJournalEntries(new HashSet<>());
        } else {
            transaction = purchaseOrder.getTransaction();
        }

        final AtomicInteger maxEntry = getMaxEntry(transaction);
        final PurchaseInvoice unsavedPurchaseInvoice = filterUnsavedPurchaseInvoices(purchaseOrder).get(0);
        final BigDecimal totalPurchases = sumTotalPurchases(unsavedPurchaseInvoice);
        unsavedPurchaseInvoice.setTotalPrice(totalPurchases);
        final Account purchasesAccount = getAccount(chartOfAccounts, PURCHASES.getType(), EXPENSES);
        final JournalEntry purchasesJournalEntry = createJournalEntry(purchasesAccount, totalPurchases, DEBIT, maxEntry, "positive");
        transaction.getJournalEntries().add(purchasesJournalEntry);

        transaction.setTransactionTypeId(savedPurchaseOrder.getId());
        final Account prepaymentAccount = getAccount(chartOfAccounts, PREPAYMENT.getType(), CASH_AND_CASH_EQUIVALENTS);
        final Account tradeCreditorsAccount = getAccount(chartOfAccounts, TRADE_CREDITORS.getType(), CURRENT_LIABILITIES);

        final List<JournalEntry> unsavedJournalEntries = filterUnsavedJournalEntries(transaction);
        BigDecimal tradeCreditors = getJournalEntriesTotalForTransactionSide(new HashSet<>(unsavedJournalEntries), DEBIT);

        final BigDecimal prepayment = (savedTransaction != null) ?
                getJournalEntriesTotalForAccount(savedTransaction.getJournalEntries(), prepaymentAccount) : ZERO;

        if (prepayment.compareTo(ZERO) > 0) {
            BigDecimal redeemAdvancePaymentForGoods;
            if (prepayment.compareTo(tradeCreditors) < 0) {
                redeemAdvancePaymentForGoods = prepayment;
                tradeCreditors = tradeCreditors.subtract(prepayment);

                JournalEntry prepaymentJournalEntry = createJournalEntry(prepaymentAccount, tradeCreditors, DEBIT, maxEntry, "positive");
                transaction.getJournalEntries().add(prepaymentJournalEntry);
            } else if (prepayment.compareTo(tradeCreditors) > 0) {
                redeemAdvancePaymentForGoods = tradeCreditors;
            } else {
                redeemAdvancePaymentForGoods = prepayment;
            }

            JournalEntry tradeCreditorsJournalEntry = createJournalEntry(tradeCreditorsAccount, redeemAdvancePaymentForGoods, DEBIT, maxEntry, "negative");
            transaction.getJournalEntries().add(tradeCreditorsJournalEntry);
        } else {
            JournalEntry tradeCreditorsJournalEntry = createJournalEntry(tradeCreditorsAccount, tradeCreditors, DEBIT, maxEntry, "positive");
            transaction.getJournalEntries().add(tradeCreditorsJournalEntry);
        }

        return saveTransaction(transaction);
    }

    public Transaction computeSalesInvoicesTransaction(SalesOrder salesOrder, SalesOrder savedSalesOrder) {
        final Transaction savedTransaction = savedSalesOrder.getTransaction();
        final ChartOfAccounts chartOfAccounts = savedSalesOrder.getCustomer().getPrincipalCompany().getChartOfAccounts();
        final Transaction transaction;
        if (salesOrder.getTransaction() == null) {
            transaction = new Transaction();
            transaction.setTransactionType(SALES_ORDER);
            transaction.setTransactionTypeId(salesOrder.getId());
            transaction.setJournalEntries(new HashSet<>());
        } else {
            transaction = salesOrder.getTransaction();
        }

        final AtomicInteger maxEntry = getMaxEntry(transaction);
        final SalesInvoice unsavedSalesInvoice = filterUnsavedSalesInvoices(salesOrder).get(0);
        final BigDecimal totalSales = sumTotalSales(unsavedSalesInvoice);
        unsavedSalesInvoice.setTotalPrice(totalSales);
        final Account salesIncomeAccount = getAccount(chartOfAccounts, SALES_INCOME.getType(), INCOME_REVENUE);
        final JournalEntry salesIncomeJournalEntry = createJournalEntry(salesIncomeAccount, totalSales, CREDIT, maxEntry, "negative");
        transaction.getJournalEntries().add(salesIncomeJournalEntry);

        transaction.setTransactionTypeId(savedSalesOrder.getId());
        final Account prepaymentAccount = getAccount(chartOfAccounts, PREPAYMENT.getType(), CASH_AND_CASH_EQUIVALENTS);
        final Account tradeCreditorsAccount = getAccount(chartOfAccounts, TRADE_DEBTORS.getType(), CURRENT_ASSETS);

        final List<JournalEntry> unsavedJournalEntries = filterUnsavedJournalEntries(transaction);
        BigDecimal tradeCreditors = getJournalEntriesTotalForTransactionSide(new HashSet<>(unsavedJournalEntries), DEBIT);

        final BigDecimal prepayment = (savedTransaction != null) ?
                getJournalEntriesTotalForAccount(savedTransaction.getJournalEntries(), prepaymentAccount) : ZERO;

        if (prepayment.compareTo(ZERO) > 0) {
            BigDecimal redeemAdvancePaymentForGoods;
            if (prepayment.compareTo(tradeCreditors) < 0) {
                redeemAdvancePaymentForGoods = prepayment;
                tradeCreditors = tradeCreditors.subtract(prepayment);

                JournalEntry prepaymentJournalEntry = createJournalEntry(prepaymentAccount, tradeCreditors, DEBIT, maxEntry, "positive");
                transaction.getJournalEntries().add(prepaymentJournalEntry);
            } else if (prepayment.compareTo(tradeCreditors) > 0) {
                redeemAdvancePaymentForGoods = tradeCreditors;
            } else {
                redeemAdvancePaymentForGoods = prepayment;
            }

            JournalEntry tradeCreditorsJournalEntry = createJournalEntry(tradeCreditorsAccount, redeemAdvancePaymentForGoods, DEBIT, maxEntry, "negative");
            transaction.getJournalEntries().add(tradeCreditorsJournalEntry);
        } else {
            JournalEntry tradeCreditorsJournalEntry = createJournalEntry(tradeCreditorsAccount, tradeCreditors, DEBIT, maxEntry, "positive");
            transaction.getJournalEntries().add(tradeCreditorsJournalEntry);
        }

        return saveTransaction(transaction);
    }

    public Transaction computeSalesOrderPaymentTransaction(SalesOrder salesOrder, SalesOrder savedSalesOrder) {
        final Transaction savedTransaction = savedSalesOrder.getTransaction();
        final ChartOfAccounts chartOfAccounts = savedSalesOrder.getCustomer().getPrincipalCompany().getChartOfAccounts();
        final Transaction transaction = salesOrder.getTransaction();
        transaction.setTransactionTypeId(savedSalesOrder.getId());

        final Account tradeDebtorsAccount = getAccount(chartOfAccounts, TRADE_DEBTORS.getType(), CURRENT_ASSETS);
        final Account prepaymentAccount = getAccount(chartOfAccounts, PREPAYMENT.getType(), CASH_AND_CASH_EQUIVALENTS);
        final AtomicInteger maxEntry = getMaxEntry(transaction);

        final List<JournalEntry> unsavedJournalEntries = filterUnsavedJournalEntries(transaction);
        BigDecimal prepayment = getJournalEntriesTotalForTransactionSide(new HashSet<>(unsavedJournalEntries), CREDIT);

        final BigDecimal tradeDebtors = (savedTransaction != null) ?
                getJournalEntriesTotalForAccount(savedTransaction.getJournalEntries(), tradeDebtorsAccount) : ZERO;

        if (tradeDebtors.compareTo(ZERO) > 0) {
            BigDecimal paymentForGoods;
            if (tradeDebtors.compareTo(prepayment) < 0) {
                paymentForGoods = tradeDebtors;
                prepayment = prepayment.subtract(tradeDebtors);

                JournalEntry prepaymentJournalEntry = createJournalEntry(prepaymentAccount, prepayment, CREDIT, maxEntry, "positive");
                transaction.getJournalEntries().add(prepaymentJournalEntry);
            } else if (tradeDebtors.compareTo(prepayment) > 0) {
                paymentForGoods = prepayment;
            } else {
                paymentForGoods = tradeDebtors;
            }

            JournalEntry tradeCreditorsJournalEntry = createJournalEntry(tradeDebtorsAccount, paymentForGoods, CREDIT, maxEntry, "negative");
            transaction.getJournalEntries().add(tradeCreditorsJournalEntry);

        } else {
            JournalEntry prepaymentJournalEntry = createJournalEntry(prepaymentAccount, prepayment, CREDIT, maxEntry, "positive");
            transaction.getJournalEntries().add(prepaymentJournalEntry);
        }
        return saveTransaction(transaction);
    }

    public List<Transaction> getAccountTransactions(UUID accountId) {
        return transactionRepository.findByJournalEntriesAccountId(accountId);
    }

    @Transactional
    public Transaction saveTransaction(Transaction transaction) {
        final Set<JournalEntry> journalEntries = transaction.getJournalEntries()
                .stream()
                .filter(journalEntry -> !journalEntry.getAmount().equals(ZERO))
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
                .collect(toList());
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

    private JournalEntry createJournalEntry(Account account, BigDecimal amount, TransactionSide transactionSide, AtomicInteger maxEntry, String sign) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setTransactionSide(transactionSide);
        if ("positive".equalsIgnoreCase(sign)) {
            journalEntry.setAmount(amount.abs());
        } else {
            journalEntry.setAmount(amount.abs().negate());
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
                .orElse(new AtomicInteger());
    }

    public List<JournalEntry> filterJournalEntriesByAccountCategory(Set<JournalEntry> journalEntries, AccountCategory accountCategory) {
        return journalEntries
                .stream()
                .filter(journalEntry -> Objects.nonNull(journalEntry.getAccount().getAccountCategory()))
                .filter(journalEntry -> journalEntry.getAccount().getAccountCategory().equals(accountCategory))
                .collect(toList());
    }

    public BigDecimal sumJournalEntriesAmount(List<JournalEntry> journalEntries) {
        return journalEntries
                .stream()
                .map(JournalEntry::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal getJournalEntriesTotalForAccount(Set<JournalEntry> journalEntries, Account account) {
        return journalEntries
                .stream()
                .filter(journalEntry -> journalEntry.getAccount().getId().equals(account.getId()))
                .map(JournalEntry::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    public BigDecimal getJournalEntriesTotalForTransactionSide(Set<JournalEntry> journalEntries, TransactionSide transactionSide) {
        return journalEntries
                .stream()
                .filter(journalEntry -> journalEntry.getTransactionSide().equals(transactionSide))
                .map(JournalEntry::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    private Account getAccount(ChartOfAccounts chartOfAccounts, String accountName, AccountType accountType1) {
        return accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
                chartOfAccounts, accountName, accountType1, null)
                .orElseThrow(NullPointerException::new);
    }

    private List<JournalEntry> filterUnsavedJournalEntries(Transaction transaction) {
        return transaction.getJournalEntries().stream()
                .filter(journalEntry -> journalEntry.getId() == null)
                .collect(toList());
    }

    private List<PurchaseInvoice> filterUnsavedPurchaseInvoices(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getInvoices().stream()
                .filter(invoice -> invoice.getId() == null)
                .collect(toList());
    }

    private List<SalesInvoice> filterUnsavedSalesInvoices(SalesOrder salesOrder) {
        return salesOrder.getInvoices().stream()
                .filter(invoice -> invoice.getId() == null)
                .collect(toList());
    }

    private BigDecimal sumTotalPurchases(PurchaseInvoice purchaseInvoice) {
        return purchaseInvoice.getLineItems().stream()
                .map(AbstractLineItem::getSubTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal sumTotalSales(SalesInvoice salesInvoice) {
        return salesInvoice.getLineItems().stream()
                .map(AbstractLineItem::getSubTotal)
                .reduce(ZERO, BigDecimal::add);
    }
}
