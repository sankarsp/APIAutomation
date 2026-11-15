package com.spsankar.api.report;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Creates one Extent entry per test (and per data row), tags it by group and class,
 * and records pass, fail (with the error) or skip. Writes the report when the suite ends.
 */
public class ExtentTestListener implements ITestListener, ISuiteListener {

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName() + parameters(result);
        String description = result.getMethod().getDescription();

        ExtentTest test = ExtentManager.createTest(name, description == null ? "" : description);
        test.assignCategory(result.getMethod().getGroups());
        test.assignCategory(result.getTestClass().getRealClass().getSimpleName());
        test.assignDevice(Thread.currentThread().getName());
        ExtentManager.setCurrent(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log(result, Status.PASS, "Passed", ExtentColor.GREEN);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ensureTest(result);
        test.fail(MarkupHelper.createLabel("Failed", ExtentColor.RED));
        if (result.getThrowable() != null) {
            test.fail(result.getThrowable());
        }
        ExtentManager.clearCurrent();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ensureTest(result);
        test.skip(MarkupHelper.createLabel("Skipped", ExtentColor.ORANGE));
        if (result.getThrowable() != null) {
            test.skip(result.getThrowable());
        }
        ExtentManager.clearCurrent();
    }

    @Override
    public void onFinish(ISuite suite) {
        ExtentManager.flush();
    }

    private void log(ITestResult result, Status status, String label, ExtentColor color) {
        ensureTest(result).log(status, MarkupHelper.createLabel(label, color));
        ExtentManager.clearCurrent();
    }

    /** A test skipped before it started (for example, a failed setup) still needs an entry. */
    private ExtentTest ensureTest(ITestResult result) {
        ExtentTest test = ExtentManager.current();
        if (test == null) {
            onTestStart(result);
            test = ExtentManager.current();
        }
        return test;
    }

    /** Shows data-driven values in the test name, with account numbers masked. */
    private String parameters(ITestResult result) {
        Object[] params = result.getParameters();
        if (params == null || params.length == 0) {
            return "";
        }
        return Arrays.stream(params)
                .map(p -> {
                    String s = String.valueOf(p);
                    if (s.matches("\\d{5,}")) {
                        s = Masking.lastFour(s);
                    }
                    return s.length() > 40 ? s.substring(0, 37) + "..." : s;
                })
                .collect(Collectors.joining(", ", " [", "]"));
    }
}
