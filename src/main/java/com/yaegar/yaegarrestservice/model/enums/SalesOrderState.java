package com.yaegar.yaegarrestservice.model.enums;

public enum SalesOrderState {
    RAISED("RAISED"),
    APPROVED("APPROVED"),
    FLAGGED("FLAGGED"),
    SENT("SENT"),
    INVOICE_ISSUED("INVOICE ISSUED"),
    CUSTOMER_ACCEPTED("CUSTOMER ACCEPTED"),
    CUSTOMER_FLAGGED("CUSTOMER FLAGGED"),
    PAID("PAID"),
    GOODS_DELIVERED("GOODS DELIVERED");

    private final String salesOrderState;

    SalesOrderState(String salesOrderState) {
        this.salesOrderState = salesOrderState;
    }
}
