package com.yaegar.yaegarrestservice.model.enums;

public enum TransactionSide {
    DEBIT("DEBIT"), CREDIT("CREDIT");

    private final String transactionSide;

    TransactionSide(String transactionSide) {
        this.transactionSide = transactionSide;
    }
}
