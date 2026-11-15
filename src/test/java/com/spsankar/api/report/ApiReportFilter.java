package com.spsankar.api.report;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Rest-Assured filter that writes every request and response into BOTH reports:
 * the current Extent test and the current Allure test (as a step with attachments).
 * Method, URL, headers, body, status and time. Secrets and account numbers are masked first,
 * so neither report ever holds a token or a full account number.
 */
public class ApiReportFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        Response response = context.next(request, responseSpec);

        String title = request.getMethod() + " " + request.getURI() + " -> " + response.getStatusCode();
        String details = requestDetails(request);
        String requestBody = request.getBody() == null ? null : Masking.body(String.valueOf((Object) request.getBody()));
        String rawResponse = response.getBody().asString();
        String responseBody = rawResponse == null || rawResponse.isBlank() ? null
                : Masking.body(isJson(response) ? response.getBody().asPrettyString() : rawResponse);

        writeToExtent(request, response, details, requestBody, responseBody);
        writeToAllure(title, response, details, requestBody, responseBody);
        return response;
    }

    private static void writeToExtent(FilterableRequestSpecification request, Response response,
                                      String details, String requestBody, String responseBody) {
        ExtentTest test = ExtentManager.current();
        if (test == null) {
            return; // called outside a test, for example during setup
        }
        test.info("<b>" + request.getMethod() + "</b> " + escape(request.getURI()));
        test.info(MarkupHelper.createCodeBlock(details));
        if (requestBody != null && !requestBody.isBlank()) {
            test.info(MarkupHelper.createCodeBlock(requestBody, CodeLanguage.JSON));
        }
        test.info("Response: <b>" + response.getStatusCode() + "</b> in " + response.getTime() + " ms");
        if (responseBody != null) {
            test.info(MarkupHelper.createCodeBlock(responseBody,
                    isJson(response) ? CodeLanguage.JSON : CodeLanguage.XML));
        }
    }

    /** Each API call becomes an Allure step with the request and response attached. Works without AspectJ. */
    private static void writeToAllure(String title, Response response,
                                      String details, String requestBody, String responseBody) {
        try {
            Allure.step(title + " (" + response.getTime() + " ms)", () -> {
                Allure.addAttachment("Request headers", "text/plain", details, ".txt");
                if (requestBody != null && !requestBody.isBlank()) {
                    Allure.addAttachment("Request body", "application/json", requestBody, ".json");
                }
                if (responseBody != null) {
                    Allure.addAttachment("Response body", isJson(response) ? "application/json" : "text/plain",
                            responseBody, isJson(response) ? ".json" : ".txt");
                }
            });
        } catch (RuntimeException e) {
            // reporting must never fail a test
        }
    }

    private static String requestDetails(FilterableRequestSpecification request) {
        String headers = StreamSupport.stream(request.getHeaders().spliterator(), false)
                .map((Header h) -> h.getName() + ": " + Masking.header(h.getName(), h.getValue()))
                .collect(Collectors.joining("\n"));
        Map<String, String> form = request.getFormParams();
        String formText = form == null || form.isEmpty() ? "" : "\n\nForm:\n" + form.entrySet().stream()
                .map(e -> e.getKey() + "=" + (e.getKey().toLowerCase().contains("secret") ? "*****" : e.getValue()))
                .collect(Collectors.joining("\n"));
        return "Headers:\n" + headers + formText;
    }

    private static boolean isJson(Response response) {
        String type = response.getContentType();
        return type != null && type.toLowerCase().contains("json");
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("<", "&lt;").replace(">", "&gt;");
    }
}
