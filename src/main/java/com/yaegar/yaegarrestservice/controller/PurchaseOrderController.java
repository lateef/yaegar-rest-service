package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.service.PurchaseInvoiceService;
import com.yaegar.yaegarrestservice.service.PurchaseOrderService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import com.yaegar.yaegarrestservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

import static com.yaegar.yaegarrestservice.model.enums.AccountCategory.CASH;
import static com.yaegar.yaegarrestservice.model.enums.PaymentTerm.NONE;
import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderEventType.*;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/secure-api")
public class PurchaseOrderController {
    private final PurchaseInvoiceService purchaseInvoiceService;
    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final TransactionService transactionService;

    @Transactional
    @RequestMapping(value = "/save-purchase-order", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> savePurchaseOrder(@RequestBody final PurchaseOrder purchaseOrder) {
        Supplier supplier = supplierService.findById(purchaseOrder.getSupplier().getId())
                .orElseThrow(NullPointerException::new);
        purchaseOrder.setSupplier(supplier);

        final Set<PurchaseOrderLineItem> lineItems = purchaseOrderService.validateOrderLineItems(purchaseOrder.getLineItems());
        purchaseOrder.setLineItems(lineItems);

        purchaseOrder.setTotalPrice(purchaseOrderService.sumLineOrderItemsSubTotal(lineItems));
        purchaseOrder.setNumber(UUID.randomUUID());
        purchaseOrder.setPaymentTerm(NONE);
        purchaseOrder.setPaid(ZERO);
        final PurchaseOrderEvent purchaseOrderEvent = new PurchaseOrderEvent(RAISE, purchaseOrder.getDescription());
        purchaseOrder.setPurchaseOrderEvents(singleton(purchaseOrderEvent));
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(purchaseOrder);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder1));
    }

    @RequestMapping(value = "/get-purchase-orders", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<PurchaseOrder>>> getPurchaseOrders(@RequestParam final UUID companyId) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderService.getPurchaseOrders(companyId);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrders));
    }

    @Transactional
    @RequestMapping(value = "/save-purchase-order-transaction", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> saveTransaction(@RequestBody PurchaseOrder purchaseOrder) {
        PurchaseOrder savedPurchaseOrder = purchaseOrderService.getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        final String confirmationMessage = transactionService.confirmSufficientFundsOrOverdraft(purchaseOrder);
        if (!"".equals(confirmationMessage)) {
            return ResponseEntity.ok().body(singletonMap(confirmationMessage, purchaseOrder));
        }

        final Transaction transaction = transactionService.computePurchaseOrderPaymentTransaction(purchaseOrder,
                savedPurchaseOrder);
        savedPurchaseOrder.setTransaction(transaction);

        final List<JournalEntry> journalEntries = transactionService.filterJournalEntriesByAccountCategory(transaction.getJournalEntries(), CASH);
        final BigDecimal totalDebitAmount = transactionService.sumJournalEntriesAmount(journalEntries);
        savedPurchaseOrder.setPaid(totalDebitAmount.abs());

        final PurchaseOrder purchaseOrder1 = purchaseOrderService.addEvent(savedPurchaseOrder, purchaseOrder.getDescription(), PAYMENT);
        PurchaseOrder purchaseOrder2 = purchaseOrderService.savePurchaseOrder(purchaseOrder1);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder2));
    }

    @Transactional
    @RequestMapping(value = "/save-purchase-order-invoices", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> saveInvoices(@RequestBody PurchaseOrder purchaseOrder) {
        PurchaseOrder savedPurchaseOrder = purchaseOrderService.getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        final String confirmationMessage = purchaseInvoiceService.confirmValidInvoice(purchaseOrder, savedPurchaseOrder);
        if (!"".equals(confirmationMessage)) {
            return ResponseEntity.ok().body(singletonMap(confirmationMessage, purchaseOrder));
        }

        final Set<PurchaseInvoice> purchaseInvoices = purchaseInvoiceService.processInvoices(purchaseOrder.getInvoices(), savedPurchaseOrder.getInvoices());

        final Transaction transaction = transactionService.computePurchaseInvoicesTransaction(purchaseOrder,
                savedPurchaseOrder);

        savedPurchaseOrder.setInvoices(purchaseInvoices);
        savedPurchaseOrder.setTransaction(transaction);

        final List<JournalEntry> journalEntries = transactionService.filterJournalEntriesByAccountCategory(transaction.getJournalEntries(), CASH);
        final BigDecimal totalDebitAmount = transactionService.sumJournalEntriesAmount(journalEntries);
        savedPurchaseOrder.setPaid(totalDebitAmount.abs());

        final PurchaseOrder purchaseOrder1 = purchaseOrderService.addEvent(savedPurchaseOrder, purchaseOrder.getDescription(), DELIVERY);
        PurchaseOrder purchaseOrder2 = purchaseOrderService.savePurchaseOrder(purchaseOrder1);

        //TODO this should factor in delivery note if available
        purchaseInvoiceService.computeInventory(savedPurchaseOrder);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder2));
    }
}