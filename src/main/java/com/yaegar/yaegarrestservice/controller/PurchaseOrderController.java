package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.service.PurchaseInvoiceService;
import com.yaegar.yaegarrestservice.service.PurchaseOrderService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import com.yaegar.yaegarrestservice.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.GOODS_RECEIVED;
import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.PAID_IN_ADVANCE;
import static com.yaegar.yaegarrestservice.model.enums.TransactionSide.DEBIT;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.singletonMap;

@RestController
@RequestMapping(value = "/secure-api")
public class PurchaseOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderController.class);

    private final PurchaseInvoiceService purchaseInvoiceService;
    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final TransactionService transactionService;

    public PurchaseOrderController(
            PurchaseInvoiceService purchaseInvoiceService,
            PurchaseOrderService purchaseOrderService,
            SupplierService supplierService,
            TransactionService transactionService
    ) {
        this.purchaseInvoiceService = purchaseInvoiceService;
        this.purchaseOrderService = purchaseOrderService;
        this.supplierService = supplierService;
        this.transactionService = transactionService;
    }

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
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(purchaseOrder);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder1));
    }

    @RequestMapping(value = "/get-purchase-orders", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<PurchaseOrder>>> getPurchaseOrders(@RequestParam final Long companyId) {
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
        // TODO get and set purchase order state
        savedPurchaseOrder.setPurchaseOrderState(PAID_IN_ADVANCE);
        savedPurchaseOrder.setTransaction(transaction);

        final BigDecimal totalDebitAmount = transactionService.getJournalEntriesTotalForTransactionSide(
                transaction.getJournalEntries(), DEBIT);
        savedPurchaseOrder.setPaid(totalDebitAmount);
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder1));
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
        // TODO get and set purchase order state
        savedPurchaseOrder.setPurchaseOrderState(GOODS_RECEIVED);
        savedPurchaseOrder.setTransaction(transaction);
        final BigDecimal totalDebitAmount = transactionService.getJournalEntriesTotalForTransactionSide(
                transaction.getJournalEntries(), DEBIT);
        savedPurchaseOrder.setPaid(totalDebitAmount);
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder);

        //TODO this should factor in delivery note if available
        purchaseInvoiceService.computeInventory(savedPurchaseOrder.getInvoices());
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder1));
    }
}