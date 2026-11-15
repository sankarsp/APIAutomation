package com.spsankar.api.tests;

import com.spsankar.api.config.Config;
import com.spsankar.api.report.ExtentManager;
import com.spsankar.api.report.ExtentTestListener;
import com.spsankar.api.support.MockPaymentsApi;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

/**
 * Starts the mock API when env=mock, connects every test to the Extent report,
 * and prepares the Allure results folder (failure categories and run details).
 * Allure itself is connected automatically by the allure-testng dependency.
 */
@Listeners(ExtentTestListener.class)
public abstract class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void startEnvironment() {
        if (Config.isMock()) {
            MockPaymentsApi.start();
        }
        prepareAllureResults();
    }

    @AfterSuite(alwaysRun = true)
    public void stopEnvironment() {
        MockPaymentsApi.stop();
        ExtentManager.flush(); // safety net: the listener also flushes when the suite ends
    }

    private static void prepareAllureResults() {
        Path results = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
        try {
            Files.createDirectories(results);
            try (InputStream categories = BaseTest.class.getClassLoader().getResourceAsStream("categories.json")) {
                if (categories != null) {
                    Files.copy(categories, results.resolve("categories.json"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            String environment = "Environment=" + Config.env() + "\n"
                    + "Groups=" + System.getProperty("groups", "all") + "\n"
                    + "Java=" + System.getProperty("java.version") + "\n";
            Files.writeString(results.resolve("environment.properties"), environment);
        } catch (IOException e) {
            // reporting must never stop the tests
        }
    }
}
