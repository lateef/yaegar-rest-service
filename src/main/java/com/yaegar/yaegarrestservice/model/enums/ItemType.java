package com.yaegar.yaegarrestservice.model.enums;

public enum ItemType {
    PRODUCT("PRODUCT"), SERVICE("SERVICE");

    private final String type;

    ItemType(String type) {
        this.type = type;
    }
}
