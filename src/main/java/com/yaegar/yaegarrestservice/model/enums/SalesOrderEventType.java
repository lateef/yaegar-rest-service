package com.yaegar.yaegarrestservice.model.enums;

public enum SalesOrderEventType {
    RAISE("Raise"),
    PAYMENT("Payment"),
    DELIVERY("Delivery"),
    FLAGGED("Flagged"),
    APPROVED("Approved"),
    SENT("Sent"),
    CUSTOMER_FLAGGED("Customer flagged"),
    CUSTOMER_ACCEPTED("Customer accepted"),
    INVOICE_ISSUED("Invoice issued"),
    CUSTOMER_INDEBTED("Customer indebted");

    private final String salesOrderEventType;

    SalesOrderEventType(String salesOrderEventType) {
        this.salesOrderEventType = salesOrderEventType;
    }
}
