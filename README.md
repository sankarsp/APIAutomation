# Payments API Automation: Rest-Assured + TestNG + Allure and Extent reports

A clean, layered API test framework for payment APIs, built the way it is described in the post
[API Automation with Rest-Assured, Java and Maven, Explained Simply](https://paymentconsultant.top/api-automation-using-rest-assured/).

It runs out of the box: a built-in mock Payments API (WireMock) starts with the tests, so you need no server, no VPN and no credentials to try it.

## What's inside

| Part | Version | Why |
|---|---|---|
| Java | 17 | Required by Rest-Assured 6 |
| Rest-Assured | 6.3.0 | Given–When–Then API tests |
| TestNG | 7.12.0 | Groups (smoke, regression), data-driven and parallel tests |
| Allure Report | 3.0.0 | Interactive report: steps, attachments, filters, timeline, failure categories |
| Extent Reports (Spark) | 5.1.2 | Single-page HTML report, easy to save and share |
| Jackson | 2.21.4 | JSON to Java records and back |
| JSON Schema Validator | 6.3.0 | Checks the shape of every response |
| WireMock | 3.13.2 | Built-in mock Payments API |

## Run it

```bash
mvn test                      # all tests against the built-in mock API
mvn test -Dgroups=smoke       # quick checks only
mvn test -Denv=uat            # a real test environment (see "Real environments")
```

## Two reports, from the same run

| Report | Open it | Best for |
|---|---|---|
| **Allure** | `mvn allure:serve` (opens in the browser), or `mvn allure:report` then `target/site/allure-maven-plugin/index.html` | Daily work: drill into each API call as a step, filter by feature or status, see the timeline and failure categories |
| **Extent** | `target/extent-report/index.html` | Sharing: one self-contained page to email or attach to a release or audit |

Allure needs Node.js installed on your machine to build the report (`node -v`). Extent needs nothing extra.

Both reports show, for every test:

- pass, fail or skip, with the full error for failures
- the description, group (smoke or regression) and test class, for filtering
- each API call: method, URL, headers, request body, status code, response time and response body
  (in Allure as a step with the request and response attached)
- in Allure: tests grouped by epic and feature, and failures grouped into categories such as
  "Wrong status code", "Wrong response data", "Too slow" and "Cannot reach the API"
- data-driven rows as separate entries, for example `accountNumberIsNeverChanged [******5678, leading zeros]`
- run details: environment, groups, Java version and, in CI, the build number and commit

**Safe to share:** the `Authorization` header, tokens and client secrets are replaced with `*****`,
and account numbers show only the last 4 digits (`******5678`). Reports get emailed and attached to audits,
so they must never hold secrets or full account numbers.

Both reports use the same masked data, from one Rest-Assured filter (`ApiReportFilter`).

In GitHub Actions both reports are uploaded as a build artifact on every run and published to
**GitHub Pages** for `master`, with a small landing page linking to each. To turn Pages on once: *Settings > Pages > Source: GitHub Actions*.

## Project structure

```
src/test/java/com/spsankar/api/
├── config/     Config            reads env/{env}.properties; secrets from environment variables
├── auth/       TokenCache        gets the OAuth2 token once and reuses it until it expires
├── specs/      Specs             one shared request setup: base URL, JSON, token, UTF-8, logging
├── client/     PaymentsClient    request builder for the Payments API
├── models/     PaymentRequest, PaymentResponse, ApiError (Java records)
├── data/       TestData          real-world test data: leading zeros, special characters, limits
├── report/     ApiReportFilter (both reports), ExtentManager, ExtentTestListener, Masking
├── support/    MockPaymentsApi   starts the WireMock mock when env=mock
└── tests/      CreatePayment, RealWorldData, NegativePayment, Idempotency
src/test/resources/
├── env/        mock.properties, uat.properties
├── schemas/    payment-response.json
├── allure.properties, categories.json   Allure settings and failure categories
└── wiremock/mappings/   the mock API's rules
```

## What the tests cover

| Test class | What it proves |
|---|---|
| CreatePaymentTest | A valid payment is accepted; schema, status, amount, currency, account and response time are all checked; the debtor account is masked |
| RealWorldDataTest | Account numbers with leading zeros, names like O'Brien, Müller & Söhne or José, and edge amounts come back unchanged |
| NegativePaymentTest | No token gives 401, over the limit gives 422, a bad currency gives 400, and errors never leak stack traces |
| IdempotencyTest | The same idempotency key sent twice creates one payment only; different keys create two |

## Real environments

1. Copy `src/test/resources/env/uat.properties` and set your API's URLs.
2. Set the credentials as environment variables, never in files:
   `export CLIENT_ID=...` and `export CLIENT_SECRET=...`
   (in GitHub: *Settings > Secrets and variables > Actions*).
3. Check the API contract: the `Idempotency-Key` header name, status codes and error codes in the tests
   follow a common pattern; adjust them to match your API.
4. Run `mvn test -Denv=uat`. Never point this suite at production: it creates payments.

## CI

`.github/workflows/api-tests.yml`:

- pull requests run the **smoke** group
- pushes to `master`, the weekday schedule and manual runs run the full **regression**
- the Allure and Extent reports are kept as an artifact for 30 days and published to GitHub Pages
- Dependabot opens weekly pull requests to keep the libraries up to date
