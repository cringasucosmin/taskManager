package tests.ui;

import base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * LoginTests — UI tests for the login and register page (index.html).
 *
 * Setup strategy:
 * Before each test, we register a fresh user via API so the UI test
 * can focus on testing the UI behavior, not the registration flow.
 *
 * Why API for setup?
 * It's faster and more reliable than going through the UI for setup steps.
 * This is a best practice in test automation.
 */
public class LoginTests extends BaseTest {

    private LoginPage loginPage;
    private String testUsername;
    private String testPassword;

    @BeforeMethod
    public void setUpLoginTests() {
        // Configure RestAssured for API-based test data setup
        RestAssured.baseURI = ConfigReader.get("base.url");

        // Each test gets a unique user to avoid cross-test interference
        testUsername = "ui_user_" + UUID.randomUUID().toString().substring(0, 8);
        testPassword = "pass123";

        // Register via API — fast and reliable
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\"}")
        .when()
            .post("/api/auth/register");

        loginPage = new LoginPage(driver);
        loginPage.open();
    }

    @Test(groups = {"smoke", "ui"},
          description = "Logging in with valid credentials redirects to the dashboard")
    public void loginWithValidCredentials_redirectsToDashboard() {
        loginPage.loginWith(testUsername, testPassword);

        // After successful login, browser should be on the dashboard
        String currentUrl = loginPage.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("dashboard"),
            "Expected redirect to dashboard, but URL was: " + currentUrl);
    }

    @Test(groups = {"ui"},
          description = "Logging in with a wrong password shows an error message")
    public void loginWithWrongPassword_showsErrorMessage() {
        loginPage.loginWith(testUsername, "wrongpassword");

        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "Error message should be visible after wrong password");
        Assert.assertFalse(loginPage.getErrorMessage().isEmpty(),
            "Error message text should not be empty");
    }

    @Test(groups = {"ui"},
          description = "Submitting the login form with empty fields shows an error")
    public void loginWithEmptyFields_showsErrorMessage() {
        loginPage.loginWith("", "");

        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "Error message should be visible when fields are empty");
    }

    @Test(groups = {"ui"},
          description = "Registering a new user via the register form shows a success message")
    public void registerNewUser_showsSuccessMessage() {
        String newUser = "new_" + UUID.randomUUID().toString().substring(0, 8);

        loginPage.registerWith(newUser, "pass123");

        Assert.assertTrue(loginPage.isSuccessDisplayed(),
            "Success message should be visible after registration");
        Assert.assertFalse(loginPage.getSuccessMessage().isEmpty(),
            "Success message text should not be empty");
    }

    @Test(groups = {"ui"},
          description = "Logging out from the dashboard redirects back to the login page")
    public void logout_redirectsToLoginPage() {
        // Log in first
        loginPage.loginWith(testUsername, testPassword);
        // Wait for redirect to dashboard (JS-based redirect needs time)
        loginPage.waitForUrlContaining("dashboard");
        Assert.assertTrue(loginPage.getCurrentUrl().contains("dashboard"),
            "Should be on dashboard after login");

        // Click logout and wait for redirect back to login
        driver.findElement(org.openqa.selenium.By.id("logout-btn")).click();
        loginPage.waitForUrlContaining("index");

        String urlAfterLogout = loginPage.getCurrentUrl();
        Assert.assertTrue(urlAfterLogout.contains("index") || urlAfterLogout.endsWith(":8080/"),
            "Expected login page after logout, but URL was: " + urlAfterLogout);
    }
}
