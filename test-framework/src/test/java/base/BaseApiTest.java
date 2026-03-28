package base;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import utils.ConfigReader;

import static io.restassured.RestAssured.given;

/**
 * BaseApiTest — parent class for all API test classes.
 *
 * Responsibilities:
 * 1. Configures RestAssured's base URL once (before any tests in the class run)
 * 2. Provides a helper to register + login and get an auth token
 *
 * Why @BeforeClass here instead of @BeforeMethod?
 * API tests don't open a browser, so setup is much cheaper.
 * We configure RestAssured once per class, not before every single test.
 * Each test still creates its own data to remain independent.
 */
public class BaseApiTest {

    protected static String authToken;

    // Expose credentials so test classes can reference them directly
    protected static final String BASE_USERNAME = ConfigReader.get("test.username");
    protected static final String BASE_PASSWORD = ConfigReader.get("test.password");

    @BeforeClass
    public void setUpApi() {
        // Tell RestAssured where the app is running
        RestAssured.baseURI = ConfigReader.get("base.url");

        // Register the test user (ignore error if already exists)
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + BASE_USERNAME +
                  "\",\"password\":\"" + BASE_PASSWORD + "\"}")
        .when()
            .post("/api/auth/register");

        // Log in and store the token for use in tests
        authToken = obtainToken(BASE_USERNAME, BASE_PASSWORD);
    }

    /**
     * Logs in with the given credentials and returns the auth token.
     * Used in tests that need to verify login behavior.
     */
    protected String obtainToken(String username, String password) {
        Response response = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .extract().response();

        if (response.statusCode() == 200) {
            return response.jsonPath().getString("token");
        }
        return null;
    }

    /** Returns the Authorization header value for authenticated requests */
    protected String bearerToken() {
        return "Bearer " + authToken;
    }
}
