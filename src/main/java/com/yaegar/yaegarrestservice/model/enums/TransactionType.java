package com.yaegar.yaegarrestservice.model.enums;

public enum TransactionType {
    PURCHASE_ORDER("Purchase order");

    private final String type;

    TransactionType(String type) {
        this.type = type;
    }
}