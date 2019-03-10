package com.yaegar.yaegarrestservice.model.enums;

public enum PurchaseOrderState {
    RAISED("RAISED"),
    FLAGGED("FLAGGED"),
    APPROVED("APPROVED"),
    PREPAYMENT("PREPAYMENT"),
    SENT("SENT"),
    SUPPLIER_FLAGGED("SUPPLIER FLAGGED"),
    SUPPLIER_ACCEPTED("SUPPLIER ACCEPTED"),
    INVOICE_RECEIVED("INVOICE RECEIVED"),
    PAID("PAID"),
    GOODS_RECEIVED("GOODS RECEIVED");

    private final String purchaseOrderState;

    PurchaseOrderState(String purchaseOrderState) {
        this.purchaseOrderState = purchaseOrderState;
    }
}
