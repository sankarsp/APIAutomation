package com.spsankar.api.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.spsankar.api.config.Config;

/**
 * Holds the single Extent report for the run and the current test for each thread,
 * so tests can run in parallel without mixing up their logs.
 */
public final class ExtentManager {

    public static final String REPORT_PATH = "target/extent-report/index.html";

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    private ExtentManager() {
    }

    public static synchronized ExtentReports reports() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("Payments API test report");
            spark.config().setReportName("Payments API automation - Rest-Assured");
            spark.config().setEncoding("UTF-8");
            spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Environment", Config.env());
            extent.setSystemInfo("Base URL", Config.isMock() ? "built-in mock API" : Config.get("base.url"));
            extent.setSystemInfo("Groups", System.getProperty("groups", "all"));
            extent.setSystemInfo("Java", System.getProperty("java.version"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            String build = System.getenv("GITHUB_RUN_NUMBER");
            if (build != null) {
                extent.setSystemInfo("CI build", build);
                extent.setSystemInfo("Commit", String.valueOf(System.getenv("GITHUB_SHA")));
            }
        }
        return extent;
    }

    /** Creating tests is synchronized: ExtentReports is not fully thread-safe for parallel runs. */
    public static synchronized ExtentTest createTest(String name, String description) {
        return reports().createTest(name, description);
    }

    public static ExtentTest current() {
        return CURRENT.get();
    }

    static void setCurrent(ExtentTest test) {
        CURRENT.set(test);
    }

    static void clearCurrent() {
        CURRENT.remove();
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
