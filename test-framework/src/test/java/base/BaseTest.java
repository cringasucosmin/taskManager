package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ConfigReader;

/**
 * BaseTest — parent class for all UI test classes.
 *
 * Responsibilities:
 * 1. Opens the browser before each test (@BeforeMethod)
 * 2. Closes the browser after each test (@AfterMethod)
 *
 * Why @BeforeMethod instead of @BeforeClass?
 * Each test gets a fresh browser. This ensures tests are independent —
 * one test can't leave dirty state that breaks the next one.
 *
 * Why WebDriverManager?
 * It auto-downloads the correct ChromeDriver for your installed Chrome.
 * No manual driver management needed.
 */
public class BaseTest {

    // Protected so subclasses (test classes) can access the driver
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        // WebDriverManager downloads the right ChromeDriver automatically
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Run headless if the system property is set (used in CI/CD)
        String headless = System.getProperty("headless", "false");
        if ("true".equalsIgnoreCase(headless)) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
        }

        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() {
        // Always close the browser, even if the test failed
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Exposes the driver to the ExtentReportListener for screenshot capture on failure.
     * Called via reflection from the listener.
     */
    public WebDriver getDriver() {
        return driver;
    }
}
