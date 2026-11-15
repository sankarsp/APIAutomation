package com.spsankar.api.report;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hides secrets and personal data before anything is written to the report.
 * Reports get shared (email, CI artifacts, auditors), so they must never hold tokens or full account numbers.
 */
public final class Masking {

    private static final Pattern ACCOUNT_FIELD =
            Pattern.compile("(\"(?:debtorAccount|creditorAccount|accountNumber)\"\\s*:\\s*\")([^\"]*)(\")");
    private static final Pattern SECRET_FIELD =
            Pattern.compile("(\"(?:access_token|refresh_token|client_secret|password)\"\\s*:\\s*\")([^\"]*)(\")");

    private Masking() {
    }

    /** Masks secret header values such as Authorization. */
    public static String header(String name, String value) {
        if (name == null || value == null) {
            return value;
        }
        String n = name.toLowerCase();
        if (n.equals("authorization") || n.contains("token") || n.contains("api-key") || n.equals("cookie")) {
            return "*****";
        }
        return value;
    }

    /** Masks account numbers (keeps the last 4 digits) and secret values inside a JSON body. */
    public static String body(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }
        String masked = replace(ACCOUNT_FIELD, body, true);
        return replace(SECRET_FIELD, masked, false);
    }

    /** Keeps only the last 4 characters: 0012345678 becomes ******5678. */
    public static String lastFour(String value) {
        if (value == null || value.length() <= 4) {
            return value;
        }
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }

    private static String replace(Pattern pattern, String text, boolean keepLastFour) {
        Matcher m = pattern.matcher(text);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String hidden = keepLastFour ? lastFour(m.group(2)) : "*****";
            m.appendReplacement(out, Matcher.quoteReplacement(m.group(1) + hidden + m.group(3)));
        }
        m.appendTail(out);
        return out.toString();
    }
}
