package tests.api;

import base.BaseApiTest;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * AuthApiTests — tests for /api/auth/register and /api/auth/login endpoints.
 *
 * What we verify:
 * - Successful register returns 200 and a message
 * - Registering the same username twice returns 400 with an error
 * - Valid login returns 200 and a token
 * - Wrong password returns 401 with an error
 *
 * Each test uses a unique username (UUID) to avoid conflicts between tests.
 */
public class AuthApiTests extends BaseApiTest {

    // ===== REGISTER =====

    @Test(groups = {"smoke", "api"},
          description = "Registering a new user returns 200 and a success message")
    public void registerNewUser_returns200() {
        String uniqueUser = "user_" + UUID.randomUUID().toString().substring(0, 8);

        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + uniqueUser + "\",\"password\":\"pass123\"}")
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(200)
            .body("message", containsString("successfully"));
    }

    @Test(groups = {"api"},
          description = "Registering with a duplicate username returns 400 with an error")
    public void registerDuplicateUser_returns400() {
        String uniqueUser = "dup_" + UUID.randomUUID().toString().substring(0, 8);

        // First registration — should succeed
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + uniqueUser + "\",\"password\":\"pass123\"}")
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(200);

        // Second registration with same username — should fail
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + uniqueUser + "\",\"password\":\"pass123\"}")
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test(groups = {"api"},
          description = "Registering with an empty username returns 400")
    public void registerWithEmptyUsername_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"\",\"password\":\"pass123\"}")
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    // ===== LOGIN =====

    @Test(groups = {"smoke", "api"},
          description = "Login with valid credentials returns 200 and a non-empty token")
    public void loginWithValidCredentials_returnsToken() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + BASE_USERNAME + "\",\"password\":\"" + BASE_PASSWORD + "\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("token", not(emptyString()));
    }

    @Test(groups = {"api"},
          description = "Login with wrong password returns 401 with an error message")
    public void loginWithWrongPassword_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + BASE_USERNAME + "\",\"password\":\"wrongpassword\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("error", notNullValue());
    }

    @Test(groups = {"api"},
          description = "Login with a non-existent username returns 401")
    public void loginWithNonExistentUser_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"nobody_" + UUID.randomUUID() + "\",\"password\":\"pass\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("error", notNullValue());
    }
}
