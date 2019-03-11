package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Invoice;
import com.yaegar.yaegarrestservice.model.LineItem;
import com.yaegar.yaegarrestservice.model.PurchaseOrder;
import com.yaegar.yaegarrestservice.model.Supplier;
import com.yaegar.yaegarrestservice.model.Transaction;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.InvoiceService;
import com.yaegar.yaegarrestservice.service.PurchaseOrderService;
import com.yaegar.yaegarrestservice.service.SupplierService;
import com.yaegar.yaegarrestservice.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;

@RestController
public class PurchaseOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderController.class);

    private final InvoiceService invoiceService;
    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final TransactionService transactionService;

    public PurchaseOrderController(
            InvoiceService invoiceService,
            PurchaseOrderService purchaseOrderService,
            SupplierService supplierService,
            TransactionService transactionService
    ) {
        this.invoiceService = invoiceService;
        this.purchaseOrderService = purchaseOrderService;
        this.supplierService = supplierService;
        this.transactionService = transactionService;
    }

    @RequestMapping(value = {"/add-purchase-order", "/save-purchase-order"}, method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> addPurchaseOrder(
            @RequestBody final PurchaseOrder purchaseOrder,
            ModelMap model,
            HttpServletRequest httpServletRequest
    ) {
        final User user = (User) model.get("user");

        Supplier supplier = supplierService.findById(purchaseOrder.getSupplier().getId())
                .orElseThrow(NullPointerException::new);
        purchaseOrder.setSupplier(supplier);

        final List<LineItem> lineItems = purchaseOrderService.sortLineItemsIntoOrderedList(purchaseOrder.getLineItems());
        final Set<LineItem> lineItems1 = purchaseOrderService.validateLineItems(lineItems, supplier, user);
        purchaseOrder.setLineItems(lineItems1);

        purchaseOrder.setTotalPrice(purchaseOrderService.sumLineItemsSubTotal(lineItems1));
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(purchaseOrder, user);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", purchaseOrder1));
    }

    @RequestMapping(value = "/get-purchase-orders", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<PurchaseOrder>>> getPurchaseOrders(@RequestParam final Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderService.getPurchaseOrders(companyId);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", purchaseOrders));
    }

    @RequestMapping(value = "/save-purchase-order-transaction", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> saveTransaction(@RequestBody PurchaseOrder purchaseOrder,
                                                                      ModelMap model,
                                                                      HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");

        PurchaseOrder savedPurchaseOrder = purchaseOrderService
                .getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Transaction transaction = transactionService.computePaymentInAdvanceTransaction(
                purchaseOrder.getTransaction(),
                purchaseOrder.getSupplier().getPrincipalCompany().getChartOfAccounts().getId(),
                savedPurchaseOrder.getId(),
                user
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction, user);
        savedPurchaseOrder.setTransaction(transaction1);
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder, user);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", purchaseOrder1));
    }

    @Transactional
    @RequestMapping(value = "/save-purchase-order-invoices", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> saveInvoices(@RequestBody PurchaseOrder purchaseOrder,
                                                                   ModelMap model,
                                                                   HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");

        PurchaseOrder savedPurchaseOrder = purchaseOrderService
                .getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Set<Invoice> invoices = purchaseOrder.getInvoices()
                .stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Invoice::getCreatedDatetime))))
                .stream()
                .map(invoice -> {
                    final List<LineItem> lineItems = purchaseOrderService.sortLineItemsIntoOrderedList(invoice.getLineItems());
                    final Set<LineItem> lineItems1 = purchaseOrderService.validateLineItems(lineItems, purchaseOrder.getSupplier(), user);
                    invoice.setLineItems(lineItems1);
                    invoice.setTotalPrice(purchaseOrderService.sumLineItemsSubTotal(lineItems1));
                    return invoice;
                })
                .collect(Collectors.toSet());

        final List<Invoice> invoices1 = invoiceService.saveAll(invoices);

        savedPurchaseOrder.setInvoices(new HashSet<>(invoices1));

        final Transaction transaction = transactionService.computeInvoicesTransaction(
                purchaseOrder.getTransaction(),
                invoiceService.sortInvoicesByDate(savedPurchaseOrder.getInvoices()),
                purchaseOrder.getSupplier().getPrincipalCompany().getChartOfAccounts().getId(),
                savedPurchaseOrder.getId(),
                user
        );

        final Transaction transaction1 = transactionService.saveTransaction(transaction, user);
        savedPurchaseOrder.setTransaction(transaction1);

        invoiceService.computeInventory(user);

        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder, user);
        return ResponseEntity.ok().headers((HttpHeaders) model.get("headers")).body(singletonMap("success", purchaseOrder1));
    }
}
