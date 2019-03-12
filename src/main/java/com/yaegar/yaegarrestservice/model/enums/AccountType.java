package com.yaegar.yaegarrestservice.model.enums;

public enum AccountType {
    ASSETS("Assets"),
    LIABILITIES("Liabilities"),
    EQUITY("Equity"),
    INCOME_REVENUE("Income/Revenue"),
    EXPENSES("Expenses"),
    PREPAYMENT("Prepayment"),
    TRADE_DEBTORS("Trade debtors"),
    TRADE_CREDITORS("Trade creditors"),
    PURCHASES("Purchases"),
    SALES_INCOME("Sales Income");

    private final String type;

    AccountType(String type) {
        this.type = type;
    }
}
