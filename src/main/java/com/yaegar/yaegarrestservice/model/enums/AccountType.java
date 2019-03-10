package com.yaegar.yaegarrestservice.model.enums;

public enum AccountType {
    ASSETS("ASSETS"),
    LIABILITIES("LIABILITIES"),
    EQUITY("EQUITY"),
    INCOME_REVENUE("INCOME/REVENUE"),
    EXPENSES("EXPENSES");

    private final String type;

    AccountType(String type) {
        this.type = type;
    }
}
