package com.yaegar.yaegarrestservice.model.enums;

public enum PaymentType {
    PURCHASE_ORDER("PURCHASE_ORDER");

    private final String type;

    PaymentType(String type) {
        this.type = type;
    }
}
