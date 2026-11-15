package com.spsankar.api.specs;

import com.spsankar.api.auth.TokenCache;
import com.spsankar.api.config.Config;
import com.spsankar.api.report.ApiReportFilter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/** One shared setup for every test: base URL, JSON, token, safe logging and the report filter. */
public final class Specs {

    private Specs() {
    }

    /** Authenticated request to the Payments API. */
    public static RequestSpecification payments() {
        return base().addHeader("Authorization", "Bearer " + TokenCache.get()).build();
    }

    /** Request with no token, for security tests. */
    public static RequestSpecification paymentsWithoutToken() {
        return base().build();
    }

    /** Expected status code and a JSON body. */
    public static ResponseSpecification json(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .expectContentType(ContentType.JSON)
                .build();
    }

    private static RequestSpecBuilder base() {
        return new RequestSpecBuilder()
                .setBaseUri(Config.get("base.url"))
                .setContentType(ContentType.JSON)
                .setConfig(RestAssuredConfig.config()
                        // never write tokens into console logs
                        .logConfig(LogConfig.logConfig()
                                .blacklistHeader("Authorization")
                                .enableLoggingOfRequestAndResponseIfValidationFails())
                        // names like Müller or José must arrive unchanged
                        .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                // request and response go into the Extent report, with secrets masked
                .addFilter(new ApiReportFilter());
    }
}
