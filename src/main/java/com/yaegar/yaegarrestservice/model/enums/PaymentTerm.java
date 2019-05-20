package com.yaegar.yaegarrestservice.model.enums;

public enum PaymentTerm {
    NONE("None"),
    PAYMENT_IN_ADVANCE("Payment in advance"),
    PAYMENT_ON_DELIVERY("Payment on delivery"),
    PAYMENT_IN_ARREARS("Payment in arrears");

    private final String paymentTerm;

    PaymentTerm(String paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public static PaymentTerm fromString(String text) {
        for (PaymentTerm paymentTerm : PaymentTerm.values()) {
            if (paymentTerm.getPaymentTerm().equalsIgnoreCase(text)) {
                return paymentTerm;
            }
        }
        throw new IllegalArgumentException(text + " is not a valid system payment term");
    }

    public String getPaymentTerm() {
        return paymentTerm;
    }
}
