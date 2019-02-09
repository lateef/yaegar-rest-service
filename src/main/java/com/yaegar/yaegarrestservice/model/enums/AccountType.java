package com.yaegar.yaegarrestservice.model.enums;

public enum AccountType {
    REVENUE("REVENUE"), BANK("BANK");

    private final String type;

    AccountType(String type) {
        this.type = type;
    }
}
