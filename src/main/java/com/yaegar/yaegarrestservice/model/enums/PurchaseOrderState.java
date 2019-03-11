package com.yaegar.yaegarrestservice.model.enums;

public enum PurchaseOrderState {
    RAISED("Raised"),
    FLAGGED("Flagged"),
    APPROVED("Approved"),
    PAID_IN_ADVANCE("Paid in advance"),
    SENT("Sent"),
    SUPPLIER_FLAGGED("Supplier flagged"),
    SUPPLIER_ACCEPTED("Supplier accepted"),
    INVOICE_RECEIVED("Invoice received"),
    PAID("Paid"),
    GOODS_RECEIVED("Goods received");

    private final String purchaseOrderState;

    PurchaseOrderState(String purchaseOrderState) {
        this.purchaseOrderState = purchaseOrderState;
    }
}
