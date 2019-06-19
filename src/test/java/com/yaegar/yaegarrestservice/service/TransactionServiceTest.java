package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.ChartOfAccounts;
import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Customer;
import com.yaegar.yaegarrestservice.model.SalesInvoice;
import com.yaegar.yaegarrestservice.model.SalesInvoiceLineItem;
import com.yaegar.yaegarrestservice.model.SalesOrder;
import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.repository.JournalEntryRepository;
import com.yaegar.yaegarrestservice.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.CASH_AND_CASH_EQUIVALENTS;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.CURRENT_ASSETS;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.INCOME_REVENUE;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.PREPAYMENT;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.SALES_INCOME;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.TRADE_DEBTORS;
import static com.yaegar.yaegarrestservice.model.enums.TransactionType.SALES_ORDER;
import static java.math.BigDecimal.TEN;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TransactionServiceTest {

    @MockBean
    private AccountService accountService;

    @MockBean
    private DateTimeProvider dateTimeProvider;

    @MockBean
    private JournalEntryRepository journalEntryRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    private TransactionService sut;

    @Before
    public void setUp() {
        sut = new TransactionService(accountService, dateTimeProvider, journalEntryRepository, transactionRepository);
    }

    @Test
    public void findById() {
        //arrange
        final UUID id = UUID.randomUUID();

        Transaction expectedTransaction = new Transaction();
        expectedTransaction.setId(id);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(expectedTransaction));

        //act
        final Transaction transaction = sut.findById(id);

        //assert
        assertThat(transaction, is(sameBeanAs(expectedTransaction)));
    }

    @Test
    public void computePurchaseOrderPaymentTransaction() {
    }

    @Test
    public void computePurchaseInvoicesTransaction() {
    }

    @Test
    public void computeSalesInvoicesTransactionForNewSalesOrder() {
        //arrange
        final UUID salesOrderId = UUID.randomUUID();
        final SalesInvoice salesInvoice = new SalesInvoice();
        final SalesInvoiceLineItem salesInvoiceLineItem = new SalesInvoiceLineItem();
        salesInvoiceLineItem.setSubTotal(TEN);
        salesInvoice.setLineItems(singleton(salesInvoiceLineItem));

        final SalesOrder salesOrder = new SalesOrder();
        salesOrder.setId(salesOrderId);
        salesOrder.setInvoices(singleton(salesInvoice));

        final SalesOrder savedSalesOrder = new SalesOrder();

        final ChartOfAccounts chartOfAccounts = new ChartOfAccounts();
        final Company company = new Company("Company");
        company.setChartOfAccounts(chartOfAccounts);

        final Customer customer = new Customer();
        customer.setPrincipalCompany(company);

        savedSalesOrder.setCustomer(customer);

        final Account salesIncomeAccount = new Account("Sales Income");
        final Account cashAndCashEquivalentsAccount = new Account("Cash and Cash Equivalents");
        final Account tradeCreditorsAccount = new Account("Trade Creditors");

        final Transaction expectedTransaction = new Transaction();
        expectedTransaction.setTransactionType(SALES_ORDER);
        expectedTransaction.setTransactionTypeId(salesOrderId);

        when(accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(chartOfAccounts,
                SALES_INCOME.getType(),
                INCOME_REVENUE,
                null))
                .thenReturn(Optional.of(salesIncomeAccount));

        when(accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(chartOfAccounts,
                PREPAYMENT.getType(),
                CASH_AND_CASH_EQUIVALENTS,
                null))
                .thenReturn(Optional.of(cashAndCashEquivalentsAccount));

        when(accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(chartOfAccounts,
                TRADE_DEBTORS.getType(),
                CURRENT_ASSETS,
                null))
                .thenReturn(Optional.of(tradeCreditorsAccount));

        when(dateTimeProvider.now()).thenReturn(LocalDateTime.now(Clock.fixed(LocalDateTime
                .of(2019, 3, 14, 12, 28, 5)
                .toInstant(ZoneOffset.UTC), ZoneId.of("UTC")
        )));

        //act
        final Transaction transaction = sut.computeSalesInvoicesTransaction(salesOrder, savedSalesOrder);

        //assert
        assertThat(transaction, is(sameBeanAs(expectedTransaction)));
    }

    @Test
    public void computeSalesInvoicesTransactionForExistingSalesOrder() {
        //arrange
        final SalesOrder salesOrder = new SalesOrder();
        salesOrder.setInvoices(emptySet());
        final SalesOrder savedSalesOrder = new SalesOrder();

        final ChartOfAccounts chartOfAccounts = new ChartOfAccounts();
        final Company company = new Company("Company");
        company.setChartOfAccounts(chartOfAccounts);

        final Customer customer = new Customer();
        customer.setPrincipalCompany(company);

        savedSalesOrder.setCustomer(customer);

        final Transaction transaction1 = new Transaction();
        transaction1.setTransactionType(SALES_ORDER);

        final Transaction expectedTransaction = new Transaction();
        expectedTransaction.setJournalEntries(emptySet());
        expectedTransaction.setTransactionType(SALES_ORDER);

        when(accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
                chartOfAccounts, SALES_INCOME.getType(), INCOME_REVENUE, null))
                .thenReturn(Optional.of(new Account()));

        when(accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
                chartOfAccounts, PREPAYMENT.getType(), CASH_AND_CASH_EQUIVALENTS, null))
                .thenReturn(Optional.of(new Account()));

        when(accountService.findByChartOfAccountsAndNameAndAccountTypeAndAccountCategory(
                chartOfAccounts, TRADE_DEBTORS.getType(), CURRENT_ASSETS, null))
                .thenReturn(Optional.of(new Account()));

        when(transactionRepository.save(transaction1)).thenReturn(transaction1);

        when(transactionRepository.save(transaction1)).thenReturn(transaction1);

        //act
        final Transaction transaction = sut.computeSalesInvoicesTransaction(salesOrder, savedSalesOrder);

        //assert
        assertThat(transaction, is(sameBeanAs(expectedTransaction)));

    }

    @Test
    public void computeSalesOrderPaymentTransaction() {
    }

    @Test
    public void getAccountTransactions() {
    }

    @Test
    public void saveTransaction() {
    }

    @Test
    public void filterJournalEntriesByAccountCategory() {
    }

    @Test
    public void sumJournalEntriesAmount() {
    }

    @Test
    public void confirmSufficientFundsOrOverdraft() {
    }
}