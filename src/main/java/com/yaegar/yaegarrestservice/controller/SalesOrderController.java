package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.service.CustomerService;
import com.yaegar.yaegarrestservice.service.SalesInvoiceService;
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
import static com.yaegar.yaegarrestservice.model.enums.SalesOrderState.PAID_IN_ADVANCE;
import static java.util.Collections.singletonMap;

@RestController
@RequestMapping(value = "/secure-api")
public class SalesOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesOrderController.class);

    private final CustomerService customerService;
    private final DateTimeProvider dateTimeProvider;
    private final SalesInvoiceService salesInvoiceService;
    private final SalesOrderService salesOrderService;
    private final TransactionService transactionService;

    public SalesOrderController(
            DateTimeProvider dateTimeProvider,
            SalesInvoiceService salesInvoiceService,
            SalesOrderService salesOrderService,
            CustomerService customerService,
            TransactionService transactionService
    ) {
        this.dateTimeProvider = dateTimeProvider;
        this.salesInvoiceService = salesInvoiceService;
        this.salesOrderService = salesOrderService;
        this.customerService = customerService;
        this.transactionService = transactionService;
    }

    @RequestMapping(value = {"/add-sales-order", "/save-sales-order"}, method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> addSalesOrder(@RequestBody final SalesOrder salesOrder) {
        Customer customer = customerService.findById(salesOrder.getCustomer().getId())
                .orElseThrow(NullPointerException::new);
        salesOrder.setCustomer(customer);

        final List<SalesOrderLineItem> lineItems = salesOrderService.sortOrderLineItemsIntoOrderedList(salesOrder.getLineItems());
        final Set<SalesOrderLineItem> lineItems1 = salesOrderService.validateOrderLineItems(lineItems);
        salesOrder.setLineItems(lineItems1);

        salesOrder.setTotalPrice(salesOrderService.sumOrderLineItemsSubTotal(lineItems1));
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

        final Set<SalesInvoice> salesInvoices = processInvoices(salesOrder);

        final List<SalesInvoice> salesInvoices1 = salesInvoiceService.saveAll(salesInvoices);

        savedSalesOrder.setInvoices(new HashSet<>(salesInvoices1));

        final Transaction transaction = transactionService.computeSalesInvoicesTransaction(
                salesOrder.getTransaction(),
                salesInvoiceService.sortInvoicesByDate(savedSalesOrder.getInvoices()),
                salesOrder.getCustomer().getPrincipalCompany().getChartOfAccounts(),
                SALES_INCOME,
                TRADE_DEBTORS,
                savedSalesOrder.getId()
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction);
        savedSalesOrder.setTransaction(transaction1);

        //TODO this should factor in delivery note if available
        salesInvoiceService.computeInventory(savedSalesOrder.getInvoices());
        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(savedSalesOrder);
        return ResponseEntity.ok().body(singletonMap("success", salesOrder1));
    }

    private Set<SalesInvoice> processInvoices(@RequestBody SalesOrder salesOrder) {
        return salesOrder.getInvoices()
                    .stream()
                    .map(salesInvoice -> {
                        if (Objects.isNull(salesInvoice.getCreatedDatetime())) {
                            salesInvoice.setCreatedDatetime(dateTimeProvider.now());
                        }
                        return salesInvoice;
                    })
                    .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SalesInvoice::getCreatedDatetime))))
                    .stream()
                    .map(salesInvoice -> {
                        final List<SalesInvoiceLineItem> lineItems = salesOrderService.sortInvoiceLineItemsIntoOrderedList(salesInvoice.getLineItems());
                        final Set<SalesInvoiceLineItem> lineItems1 = salesOrderService.validateInvoiceLineItems(
                                lineItems);
                        salesInvoice.setLineItems(lineItems1);

                        salesInvoice.setTotalPrice(salesOrderService.sumInvoiceLineItemsSubTotal(lineItems1));
                        return salesInvoice;
                    })
                    .collect(Collectors.toSet());
    }
}
