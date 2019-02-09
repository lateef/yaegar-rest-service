package com.yaegar.yaegarrestservice.model.enums;

public enum SalesOrderState {
    RAISED("RAISED"),
    SENT("SENT"),
    ACCEPTED("ACCEPTED"),
    INVOICE_ISSUED("INVOICE ISSUED"),
    PART_PAYMENT("PART PAYMENT"),
    FULL_PAYMENT("FULL PAYMENT");

    private final String salesOrderState;

    SalesOrderState(String salesOrderState) {
        this.salesOrderState = salesOrderState;
    }
}