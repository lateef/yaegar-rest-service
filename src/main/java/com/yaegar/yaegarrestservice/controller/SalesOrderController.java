package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Customer;
import com.yaegar.yaegarrestservice.model.JournalEntry;
import com.yaegar.yaegarrestservice.model.SalesInvoice;
import com.yaegar.yaegarrestservice.model.SalesOrder;
import com.yaegar.yaegarrestservice.model.SalesOrderEvent;
import com.yaegar.yaegarrestservice.model.SalesOrderLineItem;
import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.service.CustomerService;
import com.yaegar.yaegarrestservice.service.SalesInvoiceService;
import com.yaegar.yaegarrestservice.service.SalesOrderService;
import com.yaegar.yaegarrestservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.CASH;
import static com.yaegar.yaegarrestservice.model.enums.PaymentTerm.NONE;
import static com.yaegar.yaegarrestservice.model.enums.SalesOrderEventType.DELIVERY;
import static com.yaegar.yaegarrestservice.model.enums.SalesOrderEventType.PAYMENT;
import static com.yaegar.yaegarrestservice.model.enums.SalesOrderEventType.RAISE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.singleton;
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

        final Set<SalesOrderLineItem> lineItems = salesOrderService.validateOrderLineItems(salesOrder.getLineItems());
        salesOrder.setLineItems(lineItems);

        salesOrder.setTotalPrice(salesOrderService.sumOrderLineItemsSubTotal(lineItems));
        salesOrder.setNumber(UUID.randomUUID());
        salesOrder.setPaymentTerm(NONE);salesOrder.setPaid(ZERO);
        final SalesOrderEvent salesOrderEvent = new SalesOrderEvent(RAISE, salesOrder.getDescription());
        salesOrder.setSalesOrderEvents(singleton(salesOrderEvent));
        SalesOrder salesOrder1 = salesOrderService.saveSalesOrder(salesOrder);
        return ResponseEntity.ok().body(singletonMap("success", salesOrder1));
    }

    @RequestMapping(value = "/get-sales-orders", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<SalesOrder>>> getSalesOrders(@RequestParam final UUID companyId) {
        List<SalesOrder> salesOrders = salesOrderService.getSalesOrders(companyId);
        return ResponseEntity.ok().body(singletonMap("success", salesOrders));
    }

    @Transactional
    @RequestMapping(value = "/save-sales-order-transaction", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> saveTransaction(@RequestBody SalesOrder salesOrder) {
        SalesOrder savedSalesOrder = salesOrderService.getSalesOrder(salesOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Transaction transaction = transactionService.computeSalesOrderPaymentTransaction(salesOrder, savedSalesOrder);
        savedSalesOrder.setTransaction(transaction);

        final List<JournalEntry> journalEntries = transactionService.filterJournalEntriesByAccountCategory(transaction.getJournalEntries(), CASH);
        final BigDecimal totalDebitAmount = transactionService.sumJournalEntriesAmount(journalEntries);
        savedSalesOrder.setPaid(totalDebitAmount);

        final SalesOrder salesOrder1 = salesOrderService.addEvent(savedSalesOrder, salesOrder.getDescription(), PAYMENT);
        SalesOrder salesOrder2 = salesOrderService.saveSalesOrder(salesOrder1);
        return ResponseEntity.ok().body(singletonMap("success", salesOrder2));
    }

    @Transactional
    @RequestMapping(value = "/save-sales-order-invoices", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> saveInvoices(@RequestBody SalesOrder salesOrder) {
        SalesOrder savedSalesOrder = salesOrderService.getSalesOrder(salesOrder.getId())
                .orElseThrow(NullPointerException::new);

        final String confirmationMessage = salesInvoiceService.confirmValidInvoice(salesOrder, savedSalesOrder);
        if (!"".equals(confirmationMessage)) {
            return ResponseEntity.ok().body(singletonMap(confirmationMessage, salesOrder));
        }

        final String stockAvailabilityMessage = salesInvoiceService.confirmStockAvailability(salesOrder, savedSalesOrder);
        if (!"".equals(stockAvailabilityMessage)) {
            return ResponseEntity.ok().body(singletonMap(stockAvailabilityMessage, salesOrder));
        }

        final List<SalesInvoice> salesInvoices = salesInvoiceService.processInvoices(salesOrder.getInvoices());
        savedSalesOrder.setInvoices(new HashSet<>(salesInvoices));

        final Transaction transaction = transactionService.computeSalesInvoicesTransaction(salesOrder, savedSalesOrder);
        savedSalesOrder.setTransaction(transaction);

        final List<JournalEntry> journalEntries = transactionService.filterJournalEntriesByAccountCategory(transaction.getJournalEntries(), CASH);
        final BigDecimal totalDebitAmount = transactionService.sumJournalEntriesAmount(journalEntries);
        savedSalesOrder.setPaid(totalDebitAmount.abs());

        final SalesOrder salesOrder1 = salesOrderService.addEvent(savedSalesOrder, salesOrder.getDescription(), DELIVERY);
        SalesOrder salesOrder2 = salesOrderService.saveSalesOrder(salesOrder1);

        //TODO this should factor in delivery note if available
        salesInvoiceService.computeInventory(savedSalesOrder.getInvoices());
        return ResponseEntity.ok().body(singletonMap("success", salesOrder2));
    }
}
