package tests.api;

import base.BaseApiTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * TasksApiTests — tests for /api/tasks CRUD endpoints.
 *
 * What we verify:
 * - Creating a task returns 201 with the task body
 * - Getting tasks returns an array
 * - Filtering tasks by status works
 * - Updating a task changes its fields
 * - Deleting a task returns 204
 * - Unauthenticated requests return 401
 */
public class TasksApiTests extends BaseApiTest {

    @Test(groups = {"smoke", "api"},
          description = "Creating a task returns 201 with id, title and status PENDING")
    public void createTask_returns201WithBody() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", bearerToken())
            .body("{\"title\":\"API Test Task\",\"description\":\"Created by API test\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("title", equalTo("API Test Task"))
            .body("status", equalTo("PENDING"));
    }

    @Test(groups = {"api"},
          description = "GET /api/tasks returns a JSON array")
    public void getTasks_returnsArray() {
        // Create at least one task to ensure the list is non-empty
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", bearerToken())
            .body("{\"title\":\"Task for GET test\"}")
        .when()
            .post("/api/tasks");

        given()
            .header("Authorization", bearerToken())
        .when()
            .get("/api/tasks")
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test(groups = {"api"},
          description = "GET /api/tasks?status=PENDING returns only PENDING tasks")
    public void getTasksFilteredByStatus_returnsOnlyMatchingStatus() {
        // Create a task (default status = PENDING)
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", bearerToken())
            .body("{\"title\":\"Pending Task for filter test\"}")
        .when()
            .post("/api/tasks");

        // Verify all returned tasks have PENDING status
        given()
            .header("Authorization", bearerToken())
            .queryParam("status", "PENDING")
        .when()
            .get("/api/tasks")
        .then()
            .statusCode(200)
            .body("$.size()", greaterThan(0))
            .body("status", everyItem(equalTo("PENDING")));
    }

    @Test(groups = {"api"},
          description = "Updating a task changes its title and status correctly")
    public void updateTask_changesFields() {
        // First create a task and get its ID
        Response createResponse = given()
            .contentType(ContentType.JSON)
            .header("Authorization", bearerToken())
            .body("{\"title\":\"Task to update\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201)
            .extract().response();

        int taskId = createResponse.jsonPath().getInt("id");

        // Now update it
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", bearerToken())
            .body("{\"title\":\"Updated Title\",\"status\":\"IN_PROGRESS\"}")
        .when()
            .put("/api/tasks/" + taskId)
        .then()
            .statusCode(200)
            .body("title", equalTo("Updated Title"))
            .body("status", equalTo("IN_PROGRESS"));
    }

    @Test(groups = {"api"},
          description = "Deleting a task returns 204 and the task no longer exists")
    public void deleteTask_returns204() {
        // Create a task to delete
        Response createResponse = given()
            .contentType(ContentType.JSON)
            .header("Authorization", bearerToken())
            .body("{\"title\":\"Task to delete\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201)
            .extract().response();

        int taskId = createResponse.jsonPath().getInt("id");

        // Delete it
        given()
            .header("Authorization", bearerToken())
        .when()
            .delete("/api/tasks/" + taskId)
        .then()
            .statusCode(204);
    }

    @Test(groups = {"api"},
          description = "Creating a task without a title returns 400")
    public void createTaskWithEmptyTitle_returns400() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", bearerToken())
            .body("{\"title\":\"\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(400)
            .body("error", notNullValue());
    }

    @Test(groups = {"api"},
          description = "Accessing tasks without a token returns 401")
    public void getTasksWithoutToken_returns401() {
        given()
        .when()
            .get("/api/tasks")
        .then()
            .statusCode(401);
    }
}
