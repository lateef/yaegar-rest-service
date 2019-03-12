package com.yaegar.yaegarrestservice.model.enums;

public enum SalesOrderState {
    RAISED("Raised"),
    FLAGGED("Flagged"),
    APPROVED("Approved"),
    PAID_IN_ADVANCE("Paid in advance"),
    SENT("Sent"),
    CUSTOMER_FLAGGED("Customer flagged"),
    CUSTOMER_ACCEPTED("Customer accepted"),
    GOODS_DELIVERED("Goods delivered"),
    INVOICE_ISSUED("Invoice issued"),
    CUSTOMER_INDEBTED("Customer indebted");

    private final String salesOrderState;

    SalesOrderState(String salesOrderState) {
        this.salesOrderState = salesOrderState;
    }
}
