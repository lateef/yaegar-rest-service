package com.yaegar.yaegarrestservice.model.enums;

public enum LocationType {
    OFFICE("Office"),
    STORE("Store");

    private final String type;

    LocationType(String type) {
        this.type = type;
    }
}
