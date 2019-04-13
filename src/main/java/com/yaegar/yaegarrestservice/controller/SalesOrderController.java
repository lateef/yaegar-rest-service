package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.service.CustomerService;
import com.yaegar.yaegarrestservice.service.InvoiceService;
import com.yaegar.yaegarrestservice.service.SalesOrderService;
import com.yaegar.yaegarrestservice.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.yaegar.yaegarrestservice.model.enums.AccountType.SALES_INCOME;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.TRADE_DEBTORS;
import static com.yaegar.yaegarrestservice.model.enums.InvoiceType.SALES;
import static com.yaegar.yaegarrestservice.model.enums.SalesOrderState.PAID_IN_ADVANCE;
import static java.util.Collections.singletonMap;

@RestController
@RequestMapping(value = "/secure-api")
public class SalesOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesOrderController.class);

    private final CustomerService customerService;
    private final DateTimeProvider dateTimeProvider;
    private final InvoiceService invoiceService;
    private final SalesOrderService salesOrderService;
    private final TransactionService transactionService;

    public SalesOrderController(
            DateTimeProvider dateTimeProvider,
            InvoiceService invoiceService,
            SalesOrderService salesOrderService,
            CustomerService customerService,
            TransactionService transactionService
    ) {
        this.dateTimeProvider = dateTimeProvider;
        this.invoiceService = invoiceService;
        this.salesOrderService = salesOrderService;
        this.customerService = customerService;
        this.transactionService = transactionService;
    }

    @RequestMapping(value = {"/add-sales-order", "/save-sales-order"}, method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> addSalesOrder(@RequestBody final SalesOrder salesOrder) {
        Customer customer = customerService.findById(salesOrder.getCustomer().getId())
                .orElseThrow(NullPointerException::new);
        salesOrder.setCustomer(customer);

        final List<LineItem> lineItems = salesOrderService.sortLineItemsIntoOrderedList(salesOrder.getLineItems());
        final Set<LineItem> lineItems1 = salesOrderService.validateLineItems(lineItems);
        salesOrder.setLineItems(lineItems1);

        salesOrder.setTotalPrice(salesOrderService.sumLineItemsSubTotal(lineItems1));
        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(salesOrder);
        return ResponseEntity.ok().body(singletonMap("success", salesOrder1));
    }

    @RequestMapping(value = "/get-sales-orders", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<SalesOrder>>> getSalesOrders(@RequestParam final Long companyId) {
        List<SalesOrder> salesOrders = salesOrderService.getSalesOrders(companyId);
        return ResponseEntity.ok().body(singletonMap("success", salesOrders));
    }

    @Transactional
    @RequestMapping(value = "/save-sales-order-transaction", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> saveTransaction(@RequestBody SalesOrder salesOrder) {
        SalesOrder savedSalesOrder = salesOrderService
                .getSalesOrder(salesOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Transaction transaction = transactionService.computeSalesOrderPaymentInArrearsTransaction(
                salesOrder, savedSalesOrder
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction);
        savedSalesOrder.setSalesOrderState(PAID_IN_ADVANCE);
        savedSalesOrder.setTransaction(transaction1);
        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(savedSalesOrder);
        return ResponseEntity.ok().body(singletonMap("success", salesOrder1));
    }

    @Transactional
    @RequestMapping(value = "/save-sales-order-invoices", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> saveInvoices(@RequestBody SalesOrder salesOrder) {
        SalesOrder savedSalesOrder = salesOrderService
                .getSalesOrder(salesOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Set<Invoice> invoices = processInvoices(salesOrder);

        final List<Invoice> invoices1 = invoiceService.saveAll(invoices);

        savedSalesOrder.setInvoices(new HashSet<>(invoices1));

        final Transaction transaction = transactionService.computeInvoicesTransaction(
                salesOrder.getTransaction(),
                invoiceService.sortInvoicesByDate(savedSalesOrder.getInvoices()),
                salesOrder.getCustomer().getPrincipalCompany().getChartOfAccounts(),
                SALES_INCOME,
                TRADE_DEBTORS,
                savedSalesOrder.getId()
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction);
        savedSalesOrder.setTransaction(transaction1);

        //TODO this should factor in delivery note if available
        invoiceService.computeInventory(savedSalesOrder.getInvoices());
        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(savedSalesOrder);
        return ResponseEntity.ok().body(singletonMap("success", salesOrder1));
    }

    private Set<Invoice> processInvoices(@RequestBody SalesOrder salesOrder) {
        return salesOrder.getInvoices()
                    .stream()
                    .map(invoice -> {
                        if (Objects.isNull(invoice.getCreatedDatetime())) {
                            invoice.setCreatedDatetime(dateTimeProvider.now());
                        }
                        return invoice;
                    })
                    .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Invoice::getCreatedDatetime))))
                    .stream()
                    .map(invoice -> {
                        final List<LineItem> lineItems = salesOrderService.sortLineItemsIntoOrderedList(invoice.getLineItems());
                        final Set<LineItem> lineItems1 = salesOrderService.validateLineItems(
                                lineItems);
                        invoice.setLineItems(lineItems1);
                        invoice.setInvoiceType(SALES);

                        invoice.setTotalPrice(salesOrderService.sumLineItemsSubTotal(lineItems1));
                        return invoice;
                    })
                    .collect(Collectors.toSet());
    }
}
