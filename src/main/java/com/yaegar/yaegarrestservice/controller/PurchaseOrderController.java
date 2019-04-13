package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.provider.DateTimeProvider;
import com.yaegar.yaegarrestservice.service.InvoiceService;
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
import static com.yaegar.yaegarrestservice.model.enums.InvoiceType.PURCHASE;
import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.PAID_IN_ADVANCE;
import static java.util.Collections.singletonMap;

@RestController
@RequestMapping(value = "/secure-api")
public class PurchaseOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderController.class);

    private final DateTimeProvider dateTimeProvider;
    private final InvoiceService invoiceService;
    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final TransactionService transactionService;

    public PurchaseOrderController(
            DateTimeProvider dateTimeProvider,
            InvoiceService invoiceService,
            PurchaseOrderService purchaseOrderService,
            SupplierService supplierService,
            TransactionService transactionService
    ) {
        this.dateTimeProvider = dateTimeProvider;
        this.invoiceService = invoiceService;
        this.purchaseOrderService = purchaseOrderService;
        this.supplierService = supplierService;
        this.transactionService = transactionService;
    }

    @RequestMapping(value = "/save-purchase-order", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> savePurchaseOrder(@RequestBody final PurchaseOrder purchaseOrder) {
        Supplier supplier = supplierService.findById(purchaseOrder.getSupplier().getId())
                .orElseThrow(NullPointerException::new);
        purchaseOrder.setSupplier(supplier);

        final List<LineItem> lineItems = purchaseOrderService.sortLineItemsIntoOrderedList(purchaseOrder.getLineItems());
        final Set<LineItem> lineItems1 = purchaseOrderService.validateLineItems(lineItems);
        purchaseOrder.setLineItems(lineItems1);

        purchaseOrder.setTotalPrice(purchaseOrderService.sumLineItemsSubTotal(lineItems1));
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

        final Set<Invoice> invoices = processInvoices(purchaseOrder);

        final List<Invoice> invoices1 = invoiceService.saveAll(invoices);

        savedPurchaseOrder.setInvoices(new HashSet<>(invoices1));

        final Transaction transaction = transactionService.computeInvoicesTransaction(
                purchaseOrder.getTransaction(),
                invoiceService.sortInvoicesByDate(savedPurchaseOrder.getInvoices()),
                purchaseOrder.getSupplier().getPrincipalCompany().getChartOfAccounts(),
                PURCHASES,
                PREPAYMENT,
                savedPurchaseOrder.getId()
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction);
        savedPurchaseOrder.setTransaction(transaction1);

        //TODO this should factor in delivery note if available
        invoiceService.computeInventory(savedPurchaseOrder.getInvoices());
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder);
        return ResponseEntity.ok().body(singletonMap("success", purchaseOrder1));
    }

    private Set<Invoice> processInvoices(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getInvoices()
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
                        final List<LineItem> lineItems = purchaseOrderService.sortLineItemsIntoOrderedList(invoice.getLineItems());
                        final Set<LineItem> lineItems1 = purchaseOrderService.validateLineItems(
                                lineItems);
                        invoice.setLineItems(lineItems1);
                        invoice.setInvoiceType(PURCHASE);
                        invoice.setTotalPrice(purchaseOrderService.sumLineItemsSubTotal(lineItems1));
                        return invoice;
                    })
                    .collect(Collectors.toSet());
    }
}
