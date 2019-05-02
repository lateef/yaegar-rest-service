package com.yaegar.yaegarrestservice.model.enums;

public enum AccountCategory {
    CASH("CASH"),
    PRODUCT("PRODUCT"),
    SERVICE("SERVICE"),
    PRODUCT_DISCOUNT("PRODUCT DISCOUNT"),
    SERVICE_DISCOUNT("SERVICE DISCOUNT");

    private final String type;

    AccountCategory(String type) {
        this.type = type;
    }
}
