package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.service.PurchaseInvoiceService;
import com.yaegar.yaegarrestservice.service.PurchaseOrderService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import com.yaegar.yaegarrestservice.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.yaegar.yaegarrestservice.model.enums.AccountType.PREPAYMENT;
import static com.yaegar.yaegarrestservice.model.enums.AccountType.PURCHASES;
import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.PAID_IN_ADVANCE;
import static java.util.Collections.singletonMap;

@RestController
@RequestMapping(value = "/secure-api")
public class PurchaseOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderController.class);

    private final DateTimeProvider dateTimeProvider;
    private final PurchaseInvoiceService purchaseInvoiceService;
    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final TransactionService transactionService;

    public PurchaseOrderController(
            DateTimeProvider dateTimeProvider,
            PurchaseInvoiceService purchaseInvoiceService,
            PurchaseOrderService purchaseOrderService,
            SupplierService supplierService,
            TransactionService transactionService
    ) {
        this.dateTimeProvider = dateTimeProvider;
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
    public ResponseEntity<Map<String, PurchaseOrder>> saveTransaction(@RequestBody PurchaseOrder purchaseOrder) {
        PurchaseOrder savedPurchaseOrder = purchaseOrderService
                .getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Transaction transaction = transactionService.computePurchaseOrderPaymentInAdvanceTransaction(
                purchaseOrder, savedPurchaseOrder
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction);
        savedPurchaseOrder.setPurchaseOrderState(PAID_IN_ADVANCE);
        savedPurchaseOrder.setTransaction(transaction1);
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder1));
    }

    @Transactional
    @RequestMapping(value = "/save-purchase-order-invoices", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> saveInvoices(@RequestBody PurchaseOrder purchaseOrder) {
        PurchaseOrder savedPurchaseOrder = purchaseOrderService
                .getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Set<PurchaseInvoice> invoices = processInvoices(purchaseOrder);

        final List<PurchaseInvoice> invoices1 = purchaseInvoiceService.saveAll(invoices);

        savedPurchaseOrder.setInvoices(new HashSet<>(invoices1));

        final Transaction transaction = transactionService.computePurchaseInvoicesTransaction(
                purchaseOrder.getTransaction(),
                purchaseInvoiceService.sortInvoicesByDate(savedPurchaseOrder.getInvoices()),
                purchaseOrder.getSupplier().getPrincipalCompany().getChartOfAccounts(),
                PURCHASES,
                PREPAYMENT,
                savedPurchaseOrder.getId()
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction);
        savedPurchaseOrder.setTransaction(transaction1);

        //TODO this should factor in delivery note if available
        purchaseInvoiceService.computeInventory(savedPurchaseOrder.getInvoices());
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder1));
    }

    private Set<PurchaseInvoice> processInvoices(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getInvoices()
                    .stream()
                    .map(invoice -> {
                        if (Objects.isNull(invoice.getCreatedDatetime())) {
                            invoice.setCreatedDatetime(dateTimeProvider.now());
                        }
                        return invoice;
                    })
                    .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(PurchaseInvoice::getCreatedDatetime))))
                    .stream()
                    .map(invoice -> {
                        final List<PurchaseInvoiceLineItem> lineItems = purchaseOrderService
                                .sortInvoiceLineItemsIntoOrderedList(invoice.getLineItems());
                        final Set<PurchaseInvoiceLineItem> lineItems1 = purchaseOrderService.validateInvoiceLineItems(
                                lineItems);
                        invoice.setLineItems(lineItems1);
                        invoice.setTotalPrice(purchaseOrderService.sumLineInvoiceItemsSubTotal(lineItems1));
                        return invoice;
                    })
                    .collect(Collectors.toSet());
    }
}
