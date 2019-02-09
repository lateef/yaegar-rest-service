package com.yaegar.yaegarrestservice.model.enums;

public enum OrderSupplyState {
    NO_SUPPLY("NO SUPPLY"),
    PART_SUPPLY("PART SUPPLY"),
    FULL_SUPPLY("FULL SUPPLY");

    private final String supplyState;

    OrderSupplyState(String supplyState) {
        this.supplyState = supplyState;
    }
}
