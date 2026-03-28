package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * ScreenshotUtils captures the current browser state as a Base64 string.
 *
 * Why Base64?
 * Extent Reports can embed screenshots directly into the HTML report
 * as Base64 data URIs — no need to manage separate image files.
 *
 * Used by: ExtentReportListener (called automatically on test failure)
 */
public class ScreenshotUtils {

    /**
     * Captures a screenshot and returns it as a Base64-encoded string.
     * Returns null if the driver doesn't support screenshots (e.g., headless edge cases).
     */
    public static String captureBase64(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            System.err.println("Screenshot capture failed: " + e.getMessage());
            return null;
        }
    }
}
