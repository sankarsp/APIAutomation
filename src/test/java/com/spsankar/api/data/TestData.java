package com.spsankar.api.data;

import com.spsankar.api.models.PaymentRequest;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Test data that looks like the real world, not like a demo.
 * Leading zeros, long names, special characters and limit values find bugs that neat data never will.
 */
public final class TestData {

    /** Single-payment limit used by the mock API. Keep in line with the real API's limit. */
    public static final BigDecimal SINGLE_PAYMENT_LIMIT = new BigDecimal("99999.99");

    private TestData() {
    }

    public static PaymentRequest validPayment() {
        return new PaymentRequest(
                "000123456789",
                "0012345678",
                "Acme Supplies LLC",
                new BigDecimal("250.00"),
                "USD",
                "INV-" + shortId());
    }

    /** A new idempotency key: a unique ID so the server can spot a repeated request. */
    public static String newIdempotencyKey() {
        return UUID.randomUUID().toString();
    }

    /** Account numbers that break systems that treat them as numbers. */
    public static Object[][] realWorldAccounts() {
        return new Object[][] {
                {"0012345678", "leading zeros"},
                {"000000001", "mostly zeros"},
                {"123456789012345678", "18 digits, longer than a long-safe demo value"},
                {"0", "single zero"},
        };
    }

    /** Names that break encoding, escaping or field-length rules. */
    public static Object[][] realWorldNames() {
        return new Object[][] {
                {"O'Brien Consulting", "apostrophe"},
                {"Müller & Söhne GmbH", "umlauts and ampersand"},
                {"José Núñez", "accents"},
                {"A".repeat(140), "140 characters, longer than some rails allow"},
        };
    }

    /** Amounts at and around the limits. */
    public static Object[][] acceptedAmounts() {
        return new Object[][] {
                {"0.01", "smallest amount"},
                {"100.10", "trailing zero must not be lost"},
                {SINGLE_PAYMENT_LIMIT.toPlainString(), "exactly the limit"},
        };
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
