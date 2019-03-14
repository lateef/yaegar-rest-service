package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Customer;
import com.yaegar.yaegarrestservice.model.Invoice;
import com.yaegar.yaegarrestservice.model.LineItem;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.SalesOrder;
import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.CustomerService;
import com.yaegar.yaegarrestservice.service.InvoiceService;
import com.yaegar.yaegarrestservice.service.ProductService;
import com.yaegar.yaegarrestservice.service.SalesOrderService;
import com.yaegar.yaegarrestservice.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.yaegar.yaegarrestservice.model.enums.AccountType.SALES_INCOME;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.TRADE_DEBTORS;
import static com.yaegar.yaegarrestservice.model.enums.InvoiceType.SALES;
import static com.yaegar.yaegarrestservice.model.enums.SalesOrderState.CUSTOMER_INDEBTED;
import static java.util.Collections.singletonMap;

@RestController
public class SalesOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesOrderController.class);

    private final CustomerService customerService;
    private final InvoiceService invoiceService;
    private final ProductService productService;
    private final SalesOrderService salesOrderService;
    private final TransactionService transactionService;

    public SalesOrderController(
            InvoiceService invoiceService,
            ProductService productService,
            SalesOrderService salesOrderService,
            CustomerService customerService,
            TransactionService transactionService
    ) {
        this.invoiceService = invoiceService;
        this.productService = productService;
        this.salesOrderService = salesOrderService;
        this.customerService = customerService;
        this.transactionService = transactionService;
    }

    @RequestMapping(value = {"/add-sales-order", "/save-sales-order"}, method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> addSalesOrder(@RequestBody final SalesOrder salesOrder, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        Customer customer = customerService.findById(salesOrder.getCustomer().getId())
                .orElseThrow(NullPointerException::new);
        salesOrder.setCustomer(customer);

        salesOrder.getLineItems().forEach(lineItem -> {
            Product product = productService
                    .findById(lineItem
                            .getProduct()
                            .getId())
                    .orElseThrow(NullPointerException::new);
            lineItem.setProduct(product);
        });

        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(salesOrder, user);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", salesOrder1));
    }

    @RequestMapping(value = "/get-sales-orders", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<SalesOrder>>> getSalesOrders(@RequestParam final Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        List<SalesOrder> salesOrders = salesOrderService.getSalesOrders(companyId);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", salesOrders));
    }

    @Transactional
    @RequestMapping(value = "/save-sales-order-transaction", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> saveTransaction(@RequestBody SalesOrder salesOrder,
                                                                      ModelMap model,
                                                                      HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");

        SalesOrder savedSalesOrder = salesOrderService
                .getSalesOrder(salesOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Transaction transaction = transactionService.computeSalesOrderPaymentInArrearsTransaction(
                salesOrder, savedSalesOrder, user
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction, user);
        final Set<Account> accounts = transactionService.computeAccountTotal(transaction1);
        savedSalesOrder.setSalesOrderState(CUSTOMER_INDEBTED);
        savedSalesOrder.setTransaction(transaction1);
        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(savedSalesOrder, user);
        salesOrder1.getTransaction().setAccounts(accounts);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", salesOrder1));
    }

    @Transactional
    @RequestMapping(value = "/save-sales-order-invoices", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> saveInvoices(@RequestBody SalesOrder salesOrder,
                                                                   ModelMap model,
                                                                   HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");

        SalesOrder savedSalesOrder = salesOrderService
                .getSalesOrder(salesOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Set<Invoice> invoices = salesOrder.getInvoices()
                .stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Invoice::getCreatedDatetime))))
                .stream()
                .map(invoice -> {
                    final List<LineItem> lineItems = salesOrderService.sortLineItemsIntoOrderedList(invoice.getLineItems());
                    final Set<LineItem> lineItems1 = salesOrderService.validateLineItems(
                            lineItems, salesOrder.getCustomer().getPrincipalCompany(), user);
                    invoice.setLineItems(lineItems1);
                    invoice.setInvoiceType(SALES);

                    invoice.setTotalPrice(salesOrderService.sumLineItemsSubTotal(lineItems1));
                    return invoice;
                })
                .collect(Collectors.toSet());

        final List<Invoice> invoices1 = invoiceService.saveAll(invoices);

        savedSalesOrder.setInvoices(new HashSet<>(invoices1));

        final Transaction transaction = transactionService.computeInvoicesTransaction(
                salesOrder.getTransaction(),
                invoiceService.sortInvoicesByDate(savedSalesOrder.getInvoices()),
                salesOrder.getCustomer().getPrincipalCompany().getChartOfAccounts().getId(),
                SALES_INCOME,
                TRADE_DEBTORS,
                savedSalesOrder.getId(),
                user
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction, user);
        final Set<Account> accounts = transactionService.computeAccountTotal(transaction1);
        savedSalesOrder.setTransaction(transaction1);

        //TODO this should factor in delivery note if available
        invoiceService.computeInventory(savedSalesOrder.getInvoices(), user);
        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(savedSalesOrder, user);
        salesOrder1.getTransaction().setAccounts(accounts);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", salesOrder1));
    }
}
