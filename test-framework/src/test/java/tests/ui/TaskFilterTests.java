package tests.ui;

import base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.DashboardPage;
import pages.LoginPage;
import utils.ConfigReader;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * TaskFilterTests — UI tests for filtering tasks by status.
 *
 * Strategy:
 * We create tasks with specific statuses via API (faster, more controlled),
 * then verify that the UI filter buttons show the correct results.
 *
 * This demonstrates a real-world technique: use API for test data setup,
 * use UI only for testing what the user actually sees.
 */
public class TaskFilterTests extends BaseTest {

    private DashboardPage dashboardPage;
    private String authToken;

    @BeforeMethod
    public void setUpFilterTests() {
        RestAssured.baseURI = ConfigReader.get("base.url");

        String username = "filter_" + UUID.randomUUID().toString().substring(0, 8);
        String password = "pass123";

        // Register and login via API to get token
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .when()
            .post("/api/auth/register");

        authToken = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .extract().jsonPath().getString("token");

        // Create one PENDING task (default status)
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"title\":\"Pending Task\"}")
        .when()
            .post("/api/tasks");

        // Create one IN_PROGRESS task
        String inProgressId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"title\":\"In Progress Task\"}")
        .when()
            .post("/api/tasks")
        .then()
            .extract().jsonPath().getString("id");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"status\":\"IN_PROGRESS\"}")
        .when()
            .put("/api/tasks/" + inProgressId);

        // Create one DONE task
        String doneId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"title\":\"Done Task\"}")
        .when()
            .post("/api/tasks")
        .then()
            .extract().jsonPath().getString("id");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"status\":\"DONE\"}")
        .when()
            .put("/api/tasks/" + doneId);

        // Log in via UI and wait for redirect to dashboard
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.loginWith(username, password);
        loginPage.waitForUrlContaining("dashboard");

        dashboardPage = new DashboardPage(driver);
    }

    @Test(groups = {"ui"},
          description = "Clicking 'All' filter shows all 3 tasks")
    public void filterAll_showsAllTasks() {
        dashboardPage.filterByStatus("ALL");

        Assert.assertEquals(dashboardPage.getTaskCount(), 3,
            "ALL filter should show all 3 tasks");
    }

    @Test(groups = {"ui"},
          description = "Clicking 'Pending' filter shows only PENDING tasks")
    public void filterByPending_showsOnlyPendingTasks() {
        dashboardPage.filterByStatus("PENDING");

        int count = dashboardPage.getTaskCount();
        Assert.assertTrue(count > 0, "Should have at least one PENDING task");

        // Verify every visible task has PENDING badge
        // getText() returns CSS-transformed text (uppercase due to text-transform)
        for (int i = 0; i < count; i++) {
            Assert.assertTrue(
                dashboardPage.getTaskStatus(i).equalsIgnoreCase("Pending"),
                "All visible tasks should have status Pending, got: " + dashboardPage.getTaskStatus(i));
        }
    }

    @Test(groups = {"ui"},
          description = "Clicking 'In Progress' filter shows only IN_PROGRESS tasks")
    public void filterByInProgress_showsOnlyInProgressTasks() {
        dashboardPage.filterByStatus("IN_PROGRESS");

        int count = dashboardPage.getTaskCount();
        Assert.assertTrue(count > 0, "Should have at least one IN_PROGRESS task");

        for (int i = 0; i < count; i++) {
            Assert.assertTrue(
                dashboardPage.getTaskStatus(i).equalsIgnoreCase("In Progress"),
                "All visible tasks should have status In Progress, got: " + dashboardPage.getTaskStatus(i));
        }
    }

    @Test(groups = {"ui"},
          description = "Clicking 'Done' filter shows only DONE tasks")
    public void filterByDone_showsOnlyDoneTasks() {
        dashboardPage.filterByStatus("DONE");

        int count = dashboardPage.getTaskCount();
        Assert.assertTrue(count > 0, "Should have at least one DONE task");

        for (int i = 0; i < count; i++) {
            Assert.assertTrue(
                dashboardPage.getTaskStatus(i).equalsIgnoreCase("Done"),
                "All visible tasks should have status Done, got: " + dashboardPage.getTaskStatus(i));
        }
    }

    @Test(groups = {"ui"},
          description = "Switching from a filtered view back to ALL shows all tasks again")
    public void switchingFilterBackToAll_restoresFullList() {
        // First filter by PENDING
        dashboardPage.filterByStatus("PENDING");
        int pendingCount = dashboardPage.getTaskCount();

        // Then go back to ALL
        dashboardPage.filterByStatus("ALL");
        int allCount = dashboardPage.getTaskCount();

        Assert.assertTrue(allCount > pendingCount,
            "ALL filter should show more tasks than PENDING filter alone");
        Assert.assertEquals(allCount, 3,
            "ALL filter should restore the full list of 3 tasks");
    }
}
