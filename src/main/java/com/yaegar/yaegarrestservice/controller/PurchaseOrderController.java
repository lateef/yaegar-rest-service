package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.JournalEntry;
import com.yaegar.yaegarrestservice.model.PurchaseInvoice;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.PurchaseOrderEvent;
import com.yaegar.yaegarrestservice.model.PurchaseOrderLineItem;
import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.service.PurchaseInvoiceService;
import com.yaegar.yaegarrestservice.service.PurchaseOrderService;
import com.yaegar.yaegarrestservice.service.SupplierService;
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
import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderEventType.DELIVERY;
import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderEventType.PAYMENT;
import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderEventType.RAISE;
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

        final List<PurchaseOrderLineItem> lineItems = purchaseOrderService.sortOrderLineItemsIntoOrderedList(purchaseOrder.getLineItems());
        final Set<PurchaseOrderLineItem> lineItems1 = purchaseOrderService.validateOrderLineItems(lineItems);
        purchaseOrder.setLineItems(lineItems1);

        purchaseOrder.setTotalPrice(purchaseOrderService.sumLineOrderItemsSubTotal(lineItems1));
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

        final List<PurchaseInvoice> purchaseInvoices = purchaseInvoiceService.processInvoices(purchaseOrder.getInvoices());
        savedPurchaseOrder.setInvoices(new HashSet<>(purchaseInvoices));

        final Transaction transaction = transactionService.computePurchaseInvoicesTransaction(purchaseOrder,
                savedPurchaseOrder);
        savedPurchaseOrder.setTransaction(transaction);

        final List<JournalEntry> journalEntries = transactionService.filterJournalEntriesByAccountCategory(transaction.getJournalEntries(), CASH);
        final BigDecimal totalDebitAmount = transactionService.sumJournalEntriesAmount(journalEntries);
        savedPurchaseOrder.setPaid(totalDebitAmount.abs());

        final PurchaseOrder purchaseOrder1 = purchaseOrderService.addEvent(savedPurchaseOrder, purchaseOrder.getDescription(), DELIVERY);
        PurchaseOrder purchaseOrder2 = purchaseOrderService.savePurchaseOrder(purchaseOrder1);

        //TODO this should factor in delivery note if available
        purchaseInvoiceService.computeInventory(savedPurchaseOrder.getInvoices());
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder2));
    }
}