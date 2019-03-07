package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.ProductService;
import com.yaegar.yaegarrestservice.service.PurchaseOrderService;
import com.yaegar.yaegarrestservice.service.SupplierService;
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

import static java.util.Collections.singletonMap;

@RestController
public class PurchaseOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseOrderController.class);

    private CompanyService companyService;
    private ProductService productService;
    private PurchaseOrderService purchaseOrderService;
    private SupplierService supplierService;

    public PurchaseOrderController(CompanyService companyService, ProductService productService, PurchaseOrderService purchaseOrderService, SupplierService supplierService) {
        this.companyService = companyService;
        this.productService = productService;
        this.purchaseOrderService = purchaseOrderService;
        this.supplierService = supplierService;
    }

    @RequestMapping(value = {"/add-purchase-order", "/update-purchase-order"}, method = RequestMethod.POST)
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

        PurchaseOrder purchaseOrder1 = purchaseOrderService.addPurchaseOrder(purchaseOrder, user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", purchaseOrder1));
    }

    @RequestMapping(value = "/get-purchase-orders", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<PurchaseOrder>>> getPurchaseOrders(@RequestParam final Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);
        List<PurchaseOrder> purchaseOrders = purchaseOrderService.getPurchaseOrders(companyId);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", purchaseOrders));
    }

    @RequestMapping(value = "/save-purchase-order-payments", method = RequestMethod.POST)
    public ResponseEntity<Map<String, PurchaseOrder>> addPayments(@RequestBody PurchaseOrder purchaseOrder,
                                                                  ModelMap model,
                                                                               HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);

        PurchaseOrder savedPurchaseOrder = purchaseOrderService
                .getPurchaseOrder(purchaseOrder.getId())
                .orElseThrow(NullPointerException::new);

        PurchaseOrder purchaseOrder1 = purchaseOrderService.savePayments(savedPurchaseOrder, purchaseOrder.getPayments(), user);
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
