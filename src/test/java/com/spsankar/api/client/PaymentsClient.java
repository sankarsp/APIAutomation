package com.spsankar.api.client;

import static io.restassured.RestAssured.given;

import com.spsankar.api.models.PaymentRequest;
import com.spsankar.api.specs.Specs;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

/** Request builder for the Payments API, so tests hold only what is different. */
public final class PaymentsClient {

    public static final String PAYMENTS = "/payments";
    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private PaymentsClient() {
    }

    public static ValidatableResponse create(PaymentRequest request, String idempotencyKey) {
        return send(Specs.payments(), request, idempotencyKey);
    }

    public static ValidatableResponse createWithoutToken(PaymentRequest request, String idempotencyKey) {
        return send(Specs.paymentsWithoutToken(), request, idempotencyKey);
    }

    private static ValidatableResponse send(RequestSpecification spec, PaymentRequest request, String key) {
        return given()
                .spec(spec)
                .header(IDEMPOTENCY_HEADER, key)
                .body(request)
            .when()
                .post(PAYMENTS)
            .then();
    }
}
