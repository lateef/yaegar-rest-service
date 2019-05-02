package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.PurchaseOrderLineItem;
import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.model.Transaction;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.PAID_IN_ADVANCE;
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

    @RequestMapping(value = "/save-purchase-order", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> savePurchaseOrder(@RequestBody final PurchaseOrder purchaseOrder) {
        Supplier supplier = supplierService.findById(purchaseOrder.getSupplier().getId())
                .orElseThrow(NullPointerException::new);
        purchaseOrder.setSupplier(supplier);

        final List<PurchaseOrderLineItem> lineItems = purchaseOrderService.sortOrderLineItemsIntoOrderedList(purchaseOrder.getLineItems());
        final Set<PurchaseOrderLineItem> lineItems1 = purchaseOrderService.validateOrderLineItems(lineItems);
        purchaseOrder.setLineItems(lineItems1);

        purchaseOrder.setTotalPrice(purchaseOrderService.sumLineOrderItemsSubTotal(lineItems1));
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
    public ResponseEntity<Map<String, PurchaseOrder>> saveTransaction(@RequestBody PurchaseOrder purchaseOrder) throws Exception {
        PurchaseOrder savedPurchaseOrder = purchaseOrderService.getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Transaction savedTransaction;
        if (savedPurchaseOrder.getTransaction() != null) {
            savedTransaction = transactionService.findById(savedPurchaseOrder.getTransaction().getId());
        } else {
            savedTransaction = null;
        }

        if (savedTransaction == null) {
            final Set<PurchaseOrderLineItem> purchaseOrderLineItems = purchaseOrderService
                    .validateOrderLineItems(new ArrayList<>(purchaseOrder.getLineItems()));
            final BigDecimal total = purchaseOrderService.sumLineOrderItemsSubTotal(purchaseOrderLineItems);

            if (purchaseOrder.getTotalPrice().compareTo(total) < 1) {
                final Transaction transaction = transactionService.computePurchaseOrderPaymentTransaction(
                        purchaseOrder, savedPurchaseOrder
                );

                final Transaction transaction1 = transactionService.saveTransaction(transaction);
                savedPurchaseOrder.setPurchaseOrderState(PAID_IN_ADVANCE);
                savedPurchaseOrder.setTransaction(transaction1);
            } else {
                //TODO create custom exception
                //TODO  excess should go into surplus account
                throw new Exception("Amount exceeds total payment exception");
            }
        } else {

        }

        /** TODO
         is there an existing transaction
         no existing transaction: check amount exceeds total order amount
         yes exceeds total order: for now throw amount exceed payment exception - later excess should go into surplus account
         no exceeds total order: add prepayment

         yes existing transaction: check amount exceeds balance
         yes exceeds total balance: for now throw amount exceed payment exception - later excess should go into surplus account
         no exceeds total balance: check total value of goods delivered, check total prepayments
         if outstanding goods delivered, balance purchases/trade creditor and surplus goes to prepayment
         */


        // TODO calculate and set paid up amount on purchase order

        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder1));
    }

    @Transactional
    @RequestMapping(value = "/save-purchase-order-invoices", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> saveInvoices(@RequestBody PurchaseOrder purchaseOrder) {
        PurchaseOrder savedPurchaseOrder = purchaseOrderService.getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Transaction transaction = transactionService.computePurchaseInvoicesTransaction(purchaseOrder,
                savedPurchaseOrder);
        savedPurchaseOrder.setTransaction(transaction);

        //TODO this should factor in delivery note if available
        purchaseInvoiceService.computeInventory(savedPurchaseOrder.getInvoices());

        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder1));
    }
}
