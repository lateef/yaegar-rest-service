package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.service.CustomerService;
import com.yaegar.yaegarrestservice.service.SalesInvoiceService;
import com.yaegar.yaegarrestservice.service.SalesOrderService;
import com.yaegar.yaegarrestservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.CASH;
import static com.yaegar.yaegarrestservice.model.enums.SalesOrderState.GOODS_DELIVERED;
import static com.yaegar.yaegarrestservice.model.enums.SalesOrderState.PAID_IN_ADVANCE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.singletonMap;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/secure-api")
public class SalesOrderController {
    private final CustomerService customerService;
    private final SalesInvoiceService salesInvoiceService;
    private final SalesOrderService salesOrderService;
    private final TransactionService transactionService;

    @Transactional
    @RequestMapping(value = "/save-sales-order", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> addSalesOrder(@RequestBody final SalesOrder salesOrder) {
        Customer customer = customerService.findById(salesOrder.getCustomer().getId())
                .orElseThrow(NullPointerException::new);
        salesOrder.setCustomer(customer);

        final List<SalesOrderLineItem> lineItems = salesOrderService.sortOrderLineItemsIntoOrderedList(salesOrder.getLineItems());
        final Set<SalesOrderLineItem> lineItems1 = salesOrderService.validateOrderLineItems(lineItems);
        salesOrder.setLineItems(lineItems1);

        salesOrder.setTotalPrice(salesOrderService.sumOrderLineItemsSubTotal(lineItems1));
        salesOrder.setPaid(ZERO);
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
        SalesOrder savedSalesOrder = salesOrderService.getSalesOrder(salesOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Transaction transaction = transactionService.computeSalesOrderPaymentTransaction(salesOrder, savedSalesOrder);

        // TODO get and set purchase order state
        savedSalesOrder.setSalesOrderState(PAID_IN_ADVANCE);
        savedSalesOrder.setTransaction(transaction);

        final List<JournalEntry> journalEntries = transactionService.filterJournalEntriesByAccountCategory(transaction.getJournalEntries(), CASH);
        final BigDecimal totalDebitAmount = transactionService.sumJournalEntriesAmount(journalEntries);
        savedSalesOrder.setPaid(totalDebitAmount);
        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(savedSalesOrder);
        return ResponseEntity.ok().body(singletonMap("success", salesOrder1));
    }

    @Transactional
    @RequestMapping(value = "/save-sales-order-invoices", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> saveInvoices(@RequestBody SalesOrder salesOrder) {
        SalesOrder savedSalesOrder = salesOrderService.getSalesOrder(salesOrder.getId())
                .orElseThrow(NullPointerException::new);

        final List<SalesInvoice> salesInvoices = salesInvoiceService.processInvoices(salesOrder.getInvoices());
        savedSalesOrder.setInvoices(new HashSet<>(salesInvoices));

        final Transaction transaction = transactionService.computeSalesInvoicesTransaction(salesOrder, savedSalesOrder);
        // TODO get and set sales order state
        savedSalesOrder.setSalesOrderState(GOODS_DELIVERED);
        savedSalesOrder.setTransaction(transaction);
        final List<JournalEntry> journalEntries = transactionService.filterJournalEntriesByAccountCategory(transaction.getJournalEntries(), CASH);
        final BigDecimal totalDebitAmount = transactionService.sumJournalEntriesAmount(journalEntries);
        savedSalesOrder.setPaid(totalDebitAmount.abs());
        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(savedSalesOrder);

        //TODO this should factor in delivery note if available
        salesInvoiceService.computeInventory(savedSalesOrder.getInvoices());
        return ResponseEntity.ok().body(singletonMap("success", salesOrder1));
    }
}
