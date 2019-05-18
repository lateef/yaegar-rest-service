package com.yaegar.yaegarrestservice.model.enums;

public enum PurchaseOrderEventType {
    RAISE("Raise"),
    PAYMENT("Payment"),
    DELIVERY("Delivery"),
    FLAGGED("Flagged"),
    APPROVED("Approved"),
    SENT("Sent"),
    SUPPLIER_FLAGGED("Supplier flagged"),
    SUPPLIER_ACCEPTED("Supplier accepted"),
    INVOICE_ISSUED("Invoice issued"),
    SUPPLIER_CREDIT("Supplier credit");

    private final String purchaseOrderEventType;

    PurchaseOrderEventType(String purchaseOrderEventType) {
        this.purchaseOrderEventType = purchaseOrderEventType;
    }
}
