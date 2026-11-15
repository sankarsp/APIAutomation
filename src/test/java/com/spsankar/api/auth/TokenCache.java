package com.spsankar.api.auth;

import static io.restassured.RestAssured.given;

import com.spsankar.api.config.Config;
import com.spsankar.api.report.ApiReportFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.Instant;

/**
 * Gets an OAuth2 token once (client credentials) and reuses it until close to expiry.
 * Asking for a new token in every test is slow and can hit rate limits.
 */
public final class TokenCache {

    private static String token;
    private static Instant expiresAt = Instant.EPOCH;

    private TokenCache() {
    }

    public static synchronized String get() {
        if (token == null || Instant.now().isAfter(expiresAt.minusSeconds(60))) {
            Response response = given()
                    .baseUri(Config.get("auth.url"))
                    .contentType(ContentType.URLENC)
                    .filter(new ApiReportFilter())
                    .formParam("grant_type", "client_credentials")
                    .formParam("client_id", Config.secret("client.id", "CLIENT_ID"))
                    .formParam("client_secret", Config.secret("client.secret", "CLIENT_SECRET"))
                .when()
                    .post(Config.get("auth.path"))
                .then()
                    .statusCode(200)
                    .extract().response();

            token = response.jsonPath().getString("access_token");
            expiresAt = Instant.now().plusSeconds(response.jsonPath().getLong("expires_in"));
        }
        return token;
    }

    /** For tests that need a fresh token. */
    public static synchronized void clear() {
        token = null;
        expiresAt = Instant.EPOCH;
    }
}
