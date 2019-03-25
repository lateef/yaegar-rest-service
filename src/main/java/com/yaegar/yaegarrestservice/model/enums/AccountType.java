package com.yaegar.yaegarrestservice.model.enums;

public enum AccountType {
    ASSETS("Assets"),
    LIABILITIES("Liabilities"),
    EQUITY("Equity"),
    INCOME_REVENUE("Income/Revenue"),
    EXPENSES("Expenses"),
    FIXED_ASSETS("Fixed Assets"),
    CURRENT_ASSETS("Current Assets"),
    CURRENT_LIABILITIES("Current Liabilities"),
    CASH_AND_CASH_EQUIVALENTS("Cash and Cash Equivalents"),
    CASH_IN_HAND("Cash In Hand"),
    BANK_CASH("Bank Cash"),
    MARKETABLE_SECURITIES("Marketable Securities"),
    PREPAYMENT("Prepayment"),
    TRADE_DEBTORS("Trade Debtors"),
    TRADE_CREDITORS("Trade Creditors"),
    PURCHASES("Purchases"),
    SALES_INCOME("Sales Income");

    private final String type;

    AccountType(String type) {
        this.type = type;
    }

    public static AccountType fromString(String text) {
        for (AccountType accountType : AccountType.values()) {
            if (accountType.getType().equalsIgnoreCase(text)) {
                return accountType;
            }
        }
        throw new IllegalArgumentException(text + " is not a valid system account type");
    }

    public String getType() {
        return type;
    }
}
