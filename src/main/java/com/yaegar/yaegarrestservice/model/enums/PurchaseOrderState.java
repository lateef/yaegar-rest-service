package com.yaegar.yaegarrestservice.model.enums;

public enum PurchaseOrderState {
    RAISED("RAISED"),
    APPROVED("APPROVED"),
    FLAGGED("FLAGGED"),
    SENT("SENT"),
    SUPPLIER_ACCEPTED("SUPPLIER ACCEPTED"),
    SUPPLIER_FLAGGED("SUPPLIER FLAGGED"),
    INVOICE_RECEIVED("INVOICE RECEIVED"),
    PAID("PAID"),
    GOODS_RECEIVED("GOODS RECEIVED");

    private final String purchaseOrderState;

    PurchaseOrderState(String purchaseOrderState) {
        this.purchaseOrderState = purchaseOrderState;
    }
}
