package com.spsankar.api.support;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.spsankar.api.config.Config;

/**
 * A small mock Payments API (WireMock) so the suite runs anywhere: laptop or CI, no real server needed.
 * Its rules live in src/test/resources/wiremock/mappings. Used only when env=mock.
 */
public final class MockPaymentsApi {

    private static WireMockServer server;

    private MockPaymentsApi() {
    }

    public static synchronized void start() {
        if (server != null) {
            return;
        }
        server = new WireMockServer(wireMockConfig()
                .dynamicPort()
                .usingFilesUnderClasspath("wiremock"));
        server.start();

        String url = "http://localhost:" + server.port();
        Config.set("base.url", url);
        Config.set("auth.url", url);
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
