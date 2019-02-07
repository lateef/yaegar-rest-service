package com.yaegar.yaegarrestservice.model.enums;

public enum PurchaseOrderState {
    RAISED("RAISED"),
    SENT("SENT"),
    ACCEPTED("ACCEPTED"),
    INVOICE_ISSUED("INVOICE ISSUED"),
    PART_PAYMENT("PART PAYMENT"),
    FULL_PAYMENT("FULL PAYMENT");

    private final String purchaseOrderState;

    PurchaseOrderState(String purchaseOrderState) {
        this.purchaseOrderState = purchaseOrderState;
    }
}
