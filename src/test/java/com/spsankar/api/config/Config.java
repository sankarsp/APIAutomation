package com.spsankar.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Reads settings for the chosen environment from src/test/resources/env/{env}.properties.
 * Pick the environment at run time: mvn test -Denv=uat (default: mock).
 * Any value can be overridden with -Dkey=value. Secrets come from environment variables.
 */
public final class Config {

    private static final Properties PROPS = load();

    private Config() {
    }

    public static String env() {
        return System.getProperty("env", "mock");
    }

    public static boolean isMock() {
        return "mock".equalsIgnoreCase(env());
    }

    public static String get(String key) {
        String value = System.getProperty(key, PROPS.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing config value '" + key + "' for env '" + env() + "'");
        }
        return value.trim();
    }

    /** Secret: read from an environment variable first (set by CI), then from config (mock only). */
    public static String secret(String key, String envVariable) {
        String fromEnv = System.getenv(envVariable);
        return (fromEnv != null && !fromEnv.isBlank()) ? fromEnv : get(key);
    }

    public static long getLong(String key) {
        return Long.parseLong(get(key));
    }

    /** Used by the mock server to point the tests at its random port. */
    public static void set(String key, String value) {
        System.setProperty(key, value);
    }

    private static Properties load() {
        String file = "env/" + env() + ".properties";
        Properties props = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(file)) {
            if (in == null) {
                throw new IllegalStateException("Config file not found on classpath: " + file);
            }
            props.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return props;
    }
}
