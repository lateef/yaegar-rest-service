package com.yaegar.yaegarrestservice.model.enums;

public enum AccountType {
    ASSETS("Assets"),
    LIABILITIES("Liabilities"),
    EQUITY("Equity"),
    INCOME_REVENUE("Income/Revenue"),
    EXPENSES("Expenses"),
    PREPAYMENT("Prepayment"),
    PURCHASES("Purchases");

    private final String type;

    AccountType(String type) {
        this.type = type;
    }
}
