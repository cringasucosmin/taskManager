package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * LoginPage — Page Object for index.html (the login/register page).
 *
 * POM rule: this class knows HOW to interact with the login page.
 * Tests just call methods like loginPage.loginWith("user", "pass")
 * without knowing which HTML elements are involved.
 *
 * If the HTML changes (e.g., input ID changes), we fix it HERE only,
 * not in every test that uses the login page.
 */
public class LoginPage extends BasePage {

    // ===== LOCATORS — all HTML element references are kept here =====
    private final By usernameInput      = By.id("login-username");
    private final By passwordInput      = By.id("login-password");
    private final By loginButton        = By.id("login-btn");
    private final By registerUsername   = By.id("register-username");
    private final By registerPassword   = By.id("register-password");
    private final By registerButton     = By.id("register-btn");
    private final By errorMessage       = By.id("error-message");
    private final By successMessage     = By.id("success-message");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // ===== NAVIGATION =====

    /** Opens the login page in the browser */
    public LoginPage open() {
        navigateTo("/index.html");
        return this;
    }

    // ===== ACTIONS =====

    /** Types username into the login form */
    public LoginPage enterUsername(String username) {
        type(usernameInput, username);
        return this;
    }

    /** Types password into the login form */
    public LoginPage enterPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    /** Clicks the Login button */
    public LoginPage clickLogin() {
        click(loginButton);
        return this;
    }

    /** Full login action: fills the form and submits it */
    public void loginWith(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    /** Registers a new user via the register form */
    public void registerWith(String username, String password) {
        type(registerUsername, username);
        type(registerPassword, password);
        click(registerButton);
    }

    // ===== ASSERTIONS HELPERS — used in tests to verify state =====

    /** Returns the error message text shown on failed login */
    public String getErrorMessage() {
        return getText(errorMessage);
    }

    /** Returns the success message text shown after registration */
    public String getSuccessMessage() {
        return getText(successMessage);
    }

    /** Returns true if the error message is visible */
    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    /** Returns true if the success message is visible */
    public boolean isSuccessDisplayed() {
        return isDisplayed(successMessage);
    }
}
