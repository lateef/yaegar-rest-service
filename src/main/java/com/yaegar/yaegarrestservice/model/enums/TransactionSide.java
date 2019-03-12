package com.yaegar.yaegarrestservice.model.enums;

public enum TransactionSide {
    DEBIT("Debit"), CREDIT("Credit");

    private final String transactionSide;

    TransactionSide(String transactionSide) {
        this.transactionSide = transactionSide;
    }
}
