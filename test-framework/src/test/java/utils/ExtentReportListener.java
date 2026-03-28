package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * ExtentReportListener hooks into TestNG's lifecycle via ITestListener.
 *
 * How it works:
 * TestNG calls these methods automatically at the right moments:
 *  - onStart       → create the report file
 *  - onTestStart   → create an entry for each test
 *  - onTestSuccess → mark the entry as PASSED (green)
 *  - onTestFailure → mark as FAILED (red) + attach screenshot
 *  - onTestSkipped → mark as SKIPPED (yellow)
 *  - onFinish      → write the HTML file to disk
 *
 * No test code needed — the listener does everything automatically.
 * Tests just need @Listeners(ExtentReportListener.class) on their class
 * (or declared in testng.xml, which we do).
 */
public class ExtentReportListener implements ITestListener {

    private static ExtentReports extent;

    // ThreadLocal allows parallel test execution — each thread gets its own ExtentTest
    private static final ThreadLocal<ExtentTest> testNode = new ThreadLocal<>();

    // Called once at the start of the entire test suite
    @Override
    public void onStart(ITestContext context) {
        String reportPath = "test-output/ExtentReport.html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle("Task Manager - Test Report");
        spark.config().setReportName("QA Automation Results");
        spark.config().setEncoding("UTF-8");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Project", "Task Manager");
        extent.setSystemInfo("Environment", ConfigReader.get("base.url"));
        extent.setSystemInfo("Browser", ConfigReader.get("browser"));
    }

    // Called before each test method starts
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();

        ExtentTest test = extent.createTest(testName, description);
        testNode.set(test);
    }

    // Called when a test passes
    @Override
    public void onTestSuccess(ITestResult result) {
        testNode.get().pass("Test passed");
    }

    // Called when a test fails — also captures a screenshot
    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = testNode.get();
        test.fail(result.getThrowable());

        // Try to get the WebDriver from the test instance to take a screenshot
        try {
            Object testInstance = result.getInstance();

            // UI tests extend BaseTest which has a getDriver() method
            if (testInstance instanceof base.BaseTest) {
                WebDriver driver = ((base.BaseTest) testInstance).getDriver();
                if (driver != null) {
                    String base64Screenshot = ScreenshotUtils.captureBase64(driver);
                    if (base64Screenshot != null) {
                        test.fail("Screenshot at time of failure:",
                            MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
                    }
                }
            }
        } catch (Exception e) {
            test.warning("Could not capture screenshot: " + e.getMessage());
        }
    }

    // Called when a test is skipped (e.g., dependency failed)
    @Override
    public void onTestSkipped(ITestResult result) {
        testNode.get().skip(result.getThrowable());
    }

    // Called once when the entire suite finishes — writes the HTML file
    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }

    /** Allows tests to log custom messages into the report */
    public static ExtentTest getTest() {
        return testNode.get();
    }
}
