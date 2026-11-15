package com.spsankar.api.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;

import com.spsankar.api.client.PaymentsClient;
import com.spsankar.api.data.TestData;
import com.spsankar.api.models.PaymentRequest;
import com.spsankar.api.models.PaymentResponse;
import com.spsankar.api.specs.Specs;
import org.testng.annotations.DataProvider;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;

/**
 * The same test run against awkward but realistic data.
 * This is how a dropped leading zero (0012345678 -> 12345678) gets caught before production.
 */
@Epic("Payments API")
@Feature("Real-world test data")
public class RealWorldDataTest extends BaseTest {

    @DataProvider(name = "accounts", parallel = true)
    public Object[][] accounts() {
        return TestData.realWorldAccounts();
    }

    @DataProvider(name = "names", parallel = true)
    public Object[][] names() {
        return TestData.realWorldNames();
    }

    @DataProvider(name = "amounts", parallel = true)
    public Object[][] amounts() {
        return TestData.acceptedAmounts();
    }

    @Test(dataProvider = "accounts", groups = "regression",
          description = "The creditor account number comes back exactly as sent, zeros included")
    public void accountNumberIsNeverChanged(String account, String why) {
        PaymentRequest request = TestData.validPayment().withCreditorAccount(account);

        PaymentResponse response = PaymentsClient.create(request, TestData.newIdempotencyKey())
                .spec(Specs.json(201))
                .extract().as(PaymentResponse.class);

        assertThat("account changed (" + why + ")", response.creditorAccount(), equalTo(account));
    }

    @Test(dataProvider = "names", groups = "regression",
          description = "Names with special characters arrive unchanged (encoding check)")
    public void creditorNameIsNeverChanged(String name, String why) {
        PaymentRequest request = TestData.validPayment().withCreditorName(name);

        PaymentResponse response = PaymentsClient.create(request, TestData.newIdempotencyKey())
                .spec(Specs.json(201))
                .extract().as(PaymentResponse.class);

        assertThat("name changed (" + why + ")", response.creditorName(), equalTo(name));
    }

    @Test(dataProvider = "amounts", groups = "regression",
          description = "Amounts at the edges are accepted with no rounding")
    public void amountIsNeverRounded(String amount, String why) {
        PaymentRequest request = TestData.validPayment().withAmount(amount);

        PaymentResponse response = PaymentsClient.create(request, TestData.newIdempotencyKey())
                .spec(Specs.json(201))
                .extract().as(PaymentResponse.class);

        assertThat("amount changed (" + why + ")", response.amount(), comparesEqualTo(request.amount()));
    }
}
