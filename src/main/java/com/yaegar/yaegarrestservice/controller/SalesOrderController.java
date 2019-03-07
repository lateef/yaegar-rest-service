package com.yaegar.yaegarrestservice.controller;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Customer;
import com.yaegar.yaegarrestservice.model.Product;
import com.yaegar.yaegarrestservice.model.SalesOrder;
import com.yaegar.yaegarrestservice.model.SalesOrderEvent;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.service.CompanyService;
import com.yaegar.yaegarrestservice.service.CustomerService;
import com.yaegar.yaegarrestservice.service.ProductService;
import com.yaegar.yaegarrestservice.service.SalesOrderService;
import com.yaegar.yaegarrestservice.util.AuthenticationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

@RestController
public class SalesOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SalesOrderController.class);

    private CompanyService companyService;
    private ProductService productService;
    private SalesOrderService salesOrderService;
    private CustomerService customerService;

    public SalesOrderController(CompanyService companyService, ProductService productService, SalesOrderService salesOrderService, CustomerService customerService) {
        this.companyService = companyService;
        this.productService = productService;
        this.salesOrderService = salesOrderService;
        this.customerService = customerService;
    }

    @RequestMapping(value = "/add-sales-order", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> addSalesOrder(@RequestBody final SalesOrder salesOrder, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);

        Company company = companyService.findById(salesOrder.getCompany().getId())
                .orElseThrow(NullPointerException::new);
        salesOrder.setCompany(company);

        Customer customer = customerService.findById(salesOrder.getCustomer().getId())
                .orElseThrow(NullPointerException::new);
        salesOrder.setCustomer(customer);

        salesOrder.getLineItems().forEach(lineItem -> {
            Product product = productService
                    .findById(lineItem
                            .getProduct()
                            .getId())
                    .orElseThrow(NullPointerException::new);
            product.setCompany(company);
            lineItem.setProduct(product);
        });

        SalesOrder salesOrder1 = salesOrderService.addSalesOrder(salesOrder, user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", salesOrder1));
    }

    @RequestMapping(value = "/get-sales-orders", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<SalesOrder>>> getSalesOrders(@RequestParam final Long companyId, ModelMap model, HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);
        List<SalesOrder> salesOrders = salesOrderService.getSalesOrders(companyId);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", salesOrders));
    }

    @RequestMapping(value = "/add-sales-order-activity", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> addSalesOrderActivity(@RequestBody final SalesOrderEvent salesOrderEvent,
                                                                               ModelMap model,
                                                                               HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);

        SalesOrder salesOrder = salesOrderService
                .getSalesOrder(salesOrderEvent.getSalesOrderEventId())
                .orElseThrow(NullPointerException::new);

        SalesOrder salesOrder1 = salesOrderService.addSalesOrderActivity(salesOrder, salesOrderEvent, user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", salesOrder1));
    }

    @RequestMapping(value = "/add-sales-order-supply-activity", method = RequestMethod.POST)
    public ResponseEntity<Map<String, SalesOrder>> addSalesOrderSupplyActivity(@RequestBody final SalesOrderEvent salesOrderEvent,
                                                                                     ModelMap model,
                                                                                     HttpServletRequest httpServletRequest) {
        final User user = (User) model.get("user");
        HttpHeaders headers = AuthenticationUtils.getAuthenticatedUser(user);

        SalesOrder salesOrder = salesOrderService
                .getSalesOrder(salesOrderEvent.getSalesOrderEventId())
                .orElseThrow(NullPointerException::new);

        SalesOrder salesOrder1 = salesOrderService.addSalesOrderSupplyActivity(salesOrder, salesOrderEvent, user);
        return ResponseEntity.ok().headers(headers).body(singletonMap("success", salesOrder1));
    }
}
