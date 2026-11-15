package com.spsankar.api.tests;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.spsankar.api.client.PaymentsClient;
import com.spsankar.api.data.TestData;
import com.spsankar.api.models.PaymentRequest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

/**
 * A retry must never pay twice. The caller sends an idempotency key (a unique ID per payment);
 * the same key sent again must return the first payment, not create a new one.
 * Header name and status codes differ between APIs: use the ones in your API's contract.
 */
@Epic("Payments API")
@Feature("Idempotency (no double payments)")
public class IdempotencyTest extends BaseTest {

    @Test(groups = {"smoke", "regression"},
          description = "Same request and same idempotency key: one payment only")
    public void retryDoesNotPayTwice() {
        String key = TestData.newIdempotencyKey();
        PaymentRequest request = TestData.validPayment();

        String firstId = PaymentsClient.create(request, key)
                .statusCode(201)
                .extract().path("paymentId");

        PaymentsClient.create(request, key)
                .statusCode(anyOf(is(200), is(201)))
                .body("paymentId", equalTo(firstId));
    }

    @Test(groups = "regression",
          description = "Different idempotency keys: two separate payments")
    public void differentKeysCreateDifferentPayments() {
        PaymentRequest request = TestData.validPayment();

        String firstId = PaymentsClient.create(request, TestData.newIdempotencyKey())
                .statusCode(201)
                .extract().path("paymentId");

        PaymentsClient.create(request, TestData.newIdempotencyKey())
                .statusCode(201)
                .body("paymentId", not(equalTo(firstId)));
    }
}
