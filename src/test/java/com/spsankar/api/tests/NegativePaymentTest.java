package com.spsankar.api.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsStringIgnoringCase;

import com.spsankar.api.client.PaymentsClient;
import com.spsankar.api.data.TestData;
import com.spsankar.api.models.PaymentRequest;
import com.spsankar.api.specs.Specs;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

/** In payments, what the API refuses matters as much as what it accepts. */
@Epic("Payments API")
@Feature("Refused payments")
public class NegativePaymentTest extends BaseTest {

    @Test(groups = {"smoke", "regression"},
          description = "No token: refused with 401, so only trusted callers can move money")
    public void paymentWithoutTokenIsRefused() {
        PaymentsClient.createWithoutToken(TestData.validPayment(), TestData.newIdempotencyKey())
                .spec(Specs.json(401))
                .body("code", equalTo("UNAUTHORIZED"));
    }

    @Test(groups = "regression",
          description = "Amount just over the single-payment limit is refused with 422")
    public void amountOverLimitIsRefused() {
        String overLimit = TestData.SINGLE_PAYMENT_LIMIT.add(new java.math.BigDecimal("0.01")).toPlainString();
        PaymentRequest request = TestData.validPayment().withAmount(overLimit);

        PaymentsClient.create(request, TestData.newIdempotencyKey())
                .spec(Specs.json(422))
                .body("code", equalTo("LIMIT_EXCEEDED"));
    }

    @Test(groups = "regression",
          description = "Invalid currency code is refused with 400 and a clear error")
    public void invalidCurrencyIsRefused() {
        PaymentRequest request = TestData.validPayment().withCurrency("US");

        PaymentsClient.create(request, TestData.newIdempotencyKey())
                .spec(Specs.json(400))
                .body("code", equalTo("INVALID_CURRENCY"));
    }

    @Test(groups = "regression",
          description = "Error replies never leak system details such as stack traces")
    public void errorsDoNotLeakInternals() {
        PaymentRequest request = TestData.validPayment().withCurrency("US");

        PaymentsClient.create(request, TestData.newIdempotencyKey())
                .statusCode(400)
                .body(not(containsStringIgnoringCase("exception")))
                .body(not(containsStringIgnoringCase("stacktrace")))
                .body(not(containsStringIgnoringCase("at com.")));
    }
}
