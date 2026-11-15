package com.spsankar.api.tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

import com.spsankar.api.client.PaymentsClient;
import com.spsankar.api.config.Config;
import com.spsankar.api.data.TestData;
import com.spsankar.api.models.PaymentRequest;
import com.spsankar.api.models.PaymentResponse;
import com.spsankar.api.specs.Specs;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

/** Happy path: a valid payment is accepted, and the whole response is checked, not just the status. */
@Epic("Payments API")
@Feature("Create payment")
public class CreatePaymentTest extends BaseTest {

    @Test(groups = {"smoke", "regression"},
          description = "A valid payment is accepted and the response matches what was sent")
    public void validPaymentIsAccepted() {
        PaymentRequest request = TestData.validPayment();

        PaymentResponse response = PaymentsClient.create(request, TestData.newIdempotencyKey())
                .spec(Specs.json(201))
                .body(matchesJsonSchemaInClasspath("schemas/payment-response.json"))
                .body("status", equalTo("ACCEPTED"))
                .time(lessThan(Config.getLong("max.response.ms")))
                .extract().as(PaymentResponse.class);

        assertThat("amount", response.amount(), comparesEqualTo(request.amount()));
        assertThat("currency", response.currency(), equalTo(request.currency()));
        assertThat("creditor account", response.creditorAccount(), equalTo(request.creditorAccount()));
    }

    @Test(groups = {"smoke", "regression"},
          description = "The debtor account is masked in the response: only the last 4 digits are shown")
    public void debtorAccountIsMasked() {
        PaymentRequest request = TestData.validPayment();
        String lastFour = request.debtorAccount().substring(request.debtorAccount().length() - 4);

        PaymentsClient.create(request, TestData.newIdempotencyKey())
                .spec(Specs.json(201))
                .body("debtorAccountMasked", matchesPattern("\\*+" + lastFour))
                .body(not(containsString(request.debtorAccount())));
    }
}
