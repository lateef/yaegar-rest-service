package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.service.*;
import com.yaegar.yaegarrestservice.util.AuthenticationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.yaegar.yaegarrestservice.model.enums.PurchaseOrderState.PREPAYMENT;
import static java.util.Collections.singletonMap;

@RestController
public class PurchaseOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderController.class);

    final private CompanyService companyService;
    final private ProductService productService;
    final private PurchaseOrderService purchaseOrderService;
    final private SupplierService supplierService;
    final private TransactionService transactionService;

    public PurchaseOrderController(
            CompanyService companyService,
            ProductService productService,
            PurchaseOrderService purchaseOrderService,
            SupplierService supplierService,
            TransactionService transactionService
    ) {
        this.companyService = companyService;
        this.productService = productService;
        this.purchaseOrderService = purchaseOrderService;
        this.supplierService = supplierService;
        this.transactionService = transactionService;
    }

    @RequestMapping(value = {"/add-purchase-order", "/save-purchase-order"}, method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> addPurchaseOrder(@RequestBody final PurchaseOrder purchaseOrder, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);

        Company company = companyService.findById(purchaseOrder.getCompany().getId())
                .orElseThrow(NullPointerException::new);
        purchaseOrder.setCompany(company);

        Supplier supplier = supplierService.findById(purchaseOrder.getSupplier().getId())
                .orElseThrow(NullPointerException::new);
        purchaseOrder.setSupplier(supplier);

        purchaseOrder.getLineItems().forEach(lineItem -> {
            Product product = productService
                    .findById(lineItem
                            .getProduct()
                            .getId())
                    .orElseThrow(NullPointerException::new);
            product.setCompany(company);
            lineItem.setProduct(product);
        });

        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(purchaseOrder, user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", purchaseOrder1));
    }

    @RequestMapping(value = "/get-purchase-orders", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<PurchaseOrder>>> getPurchaseOrders(@RequestParam final Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);
        List<PurchaseOrder> purchaseOrders = purchaseOrderService.getPurchaseOrders(companyId);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", purchaseOrders));
    }

    @RequestMapping(value = "/save-purchase-order-transaction", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> saveTransaction(@RequestBody PurchaseOrder purchaseOrder,
                                                                      ModelMap model,
                                                                      HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);

        PurchaseOrder savedPurchaseOrder = purchaseOrderService
                .getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        final Transaction transaction = transactionService.computeTransaction(
                purchaseOrder.getTransaction(),
                purchaseOrder.getCompany().getChartOfAccounts().getId(),
                purchaseOrder.getPurchaseOrderState(),
                savedPurchaseOrder.getId(),
                user
        );
        final Transaction transaction1 = transactionService.saveTransaction(transaction, user);
        savedPurchaseOrder.setPurchaseOrderState(PREPAYMENT);
        savedPurchaseOrder.setTransaction(transaction1);
        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePurchaseOrder(savedPurchaseOrder, user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", purchaseOrder1));
    }

    @RequestMapping(value = "/save-purchase-order-invoices", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> saveInvoices(@RequestBody PurchaseOrder purchaseOrder,
                                                                   ModelMap model,
                                                                   HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);

        PurchaseOrder savedPurchaseOrder = purchaseOrderService
                .getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        PurchaseOrder purchaseOrder1 = purchaseOrderService.saveInvoices(savedPurchaseOrder, purchaseOrder.getInvoices(), user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", purchaseOrder1));
    }

    @RequestMapping(value = "/add-purchase-order-supply-activity", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> addPurchaseOrderSupplyActivity(@RequestBody final PurchaseOrderEvent purchaseOrderEvent,
                                                                                     ModelMap model,
                                                                                     HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);

        PurchaseOrder purchaseOrder = purchaseOrderService
                .getPurchaseOrder(purchaseOrderEvent.getPurchaseOrderEventId())
                .orElseThrow(NullPointerException::new);

        PurchaseOrder purchaseOrder1 = purchaseOrderService.addPurchaseOrderSupplyActivity(purchaseOrder, purchaseOrderEvent, user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", purchaseOrder1));
    }
}
