package com.yaegar.yaegarrestservice.model.enums;

public enum ProductClassifier {
    PRODUCT("PRODUCT"), DISCOUNT("DISCOUNT");

    private final String type;

    ProductClassifier(String type) {
        this.type = type;
    }
}
