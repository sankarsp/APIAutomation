package com.spsankar.api.models;

import java.math.BigDecimal;

/**
 * Request body for POST /payments.
 * Account numbers are Strings, never numbers: a number drops leading zeros (0012345678 -> 12345678).
 * Money is BigDecimal, never double: a double cannot hold 0.10 exactly.
 */
public record PaymentRequest(
        String debtorAccount,
        String creditorAccount,
        String creditorName,
        BigDecimal amount,
        String currency,
        String reference) {

    public PaymentRequest withCreditorAccount(String value) {
        return new PaymentRequest(debtorAccount, value, creditorName, amount, currency, reference);
    }

    public PaymentRequest withCreditorName(String value) {
        return new PaymentRequest(debtorAccount, creditorAccount, value, amount, currency, reference);
    }

    public PaymentRequest withAmount(String value) {
        return new PaymentRequest(debtorAccount, creditorAccount, creditorName, new BigDecimal(value), currency, reference);
    }

    public PaymentRequest withCurrency(String value) {
        return new PaymentRequest(debtorAccount, creditorAccount, creditorName, amount, value, reference);
    }
}
