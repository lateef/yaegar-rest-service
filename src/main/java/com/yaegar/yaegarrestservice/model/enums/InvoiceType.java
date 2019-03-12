package com.yaegar.yaegarrestservice.model.enums;

public enum InvoiceType {
    PURCHASE("Purchase"),
    SALES("Sales");

    private final String type;

    InvoiceType(String type) {
        this.type = type;
    }
}
