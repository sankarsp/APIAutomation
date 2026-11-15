package com.spsankar.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Response body for POST /payments. Unknown fields are ignored so new API fields don't break tests. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentResponse(
        String paymentId,
        String status,
        BigDecimal amount,
        String currency,
        String creditorAccount,
        String creditorName,
        String debtorAccountMasked,
        String createdAt) {
}
