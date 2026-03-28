package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;

/**
 * BasePage is the parent class for all Page Objects.
 *
 * Why?
 * Every page needs the WebDriver and common actions like "wait for element",
 * "click", "type text". Instead of duplicating this in every page class,
 * we put it here once and all page objects extend it.
 *
 * This is a key part of the Page Object Model pattern.
 */
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        int timeoutSeconds = ConfigReader.getInt("explicit.wait");
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    // ===== NAVIGATION =====

    /** Navigates to a path relative to the base URL (e.g., "/dashboard.html") */
    protected void navigateTo(String path) {
        driver.get(ConfigReader.get("base.url") + path);
    }

    // ===== WAIT HELPERS =====

    /** Waits until an element is visible and returns it */
    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Waits until an element is clickable and returns it */
    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Waits until an element is present in the DOM (doesn't have to be visible) */
    protected WebElement waitForPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ===== ACTION HELPERS =====

    /** Clears the field and types the given text */
    protected void type(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    /** Clicks an element after waiting for it to be clickable */
    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    /** Returns the text content of an element */
    protected String getText(By locator) {
        return waitForVisible(locator).getText();
    }

    /** Returns true if the element is currently displayed */
    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the current browser URL */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /** Returns the current page title */
    public String getPageTitle() {
        return driver.getTitle();
    }

    /** Waits until the current URL contains the given fragment */
    public void waitForUrlContaining(String fragment) {
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains(fragment));
    }
}
