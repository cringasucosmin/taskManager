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
 * TaskCRUDTests — UI tests for creating, editing and deleting tasks.
 *
 * Setup: each test registers a unique user via API, then logs in via the UI.
 * This ensures tests are fully independent from each other.
 */
public class TaskCRUDTests extends BaseTest {

    private DashboardPage dashboardPage;
    private String testUsername;
    private String testPassword;
    private String authToken;

    @BeforeMethod
    public void setUpTaskTests() {
        RestAssured.baseURI = ConfigReader.get("base.url");

        testUsername = "crud_" + UUID.randomUUID().toString().substring(0, 8);
        testPassword = "pass123";

        // Register user via API
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\"}")
        .when()
            .post("/api/auth/register");

        // Get auth token for API-based setup steps
        authToken = given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .extract().jsonPath().getString("token");

        // Log in via the UI and wait for redirect to dashboard
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.loginWith(testUsername, testPassword);
        loginPage.waitForUrlContaining("dashboard");

        dashboardPage = new DashboardPage(driver);
    }

    @Test(groups = {"ui"},
          description = "Creating a task with a valid title makes it appear in the list")
    public void createTaskWithValidData_appearsInList() {
        String taskTitle = "My Test Task " + UUID.randomUUID().toString().substring(0, 4);

        int beforeCount = dashboardPage.getTaskCount();
        dashboardPage.createTask(taskTitle, "Some description");
        int afterCount = dashboardPage.getTaskCount();

        Assert.assertEquals(afterCount, beforeCount + 1,
            "Task count should increase by 1 after creating a task");
        Assert.assertEquals(dashboardPage.getTaskTitle(0), taskTitle,
            "The created task title should match");
    }

    @Test(groups = {"ui"},
          description = "Submitting the task form with an empty title shows an error")
    public void createTaskWithEmptyTitle_showsError() {
        dashboardPage.submitEmptyTitle();

        Assert.assertTrue(dashboardPage.isTaskErrorDisplayed(),
            "Error message should appear when title is empty");
    }

    @Test(groups = {"ui"},
          description = "Editing a task updates its title in the list")
    public void editExistingTask_updatesTitleInList() {
        // Create a task first via API (faster than UI)
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"title\":\"Original Title\"}")
        .when()
            .post("/api/tasks");

        // Reload to see the task
        driver.navigate().refresh();

        String newTitle = "Updated Title " + UUID.randomUUID().toString().substring(0, 4);

        dashboardPage.clickEditOnTask(0);

        // Verify form switches to edit mode
        Assert.assertEquals(dashboardPage.getSubmitButtonText(), "Save Changes",
            "Submit button should say 'Save Changes' in edit mode");
        Assert.assertTrue(dashboardPage.isCancelButtonDisplayed(),
            "Cancel button should be visible in edit mode");

        dashboardPage.updateTaskTitle(newTitle);

        Assert.assertEquals(dashboardPage.getTaskTitle(0), newTitle,
            "Task title should be updated after editing");
    }

    @Test(groups = {"ui"},
          description = "Clicking Cancel in edit mode restores the form to its initial state")
    public void cancelEdit_resetsForm() {
        // Create a task via API
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"title\":\"Task for cancel test\"}")
        .when()
            .post("/api/tasks");

        driver.navigate().refresh();

        // Enter edit mode then cancel
        dashboardPage.clickEditOnTask(0);
        Assert.assertTrue(dashboardPage.isCancelButtonDisplayed());

        dashboardPage.cancelEdit();

        Assert.assertEquals(dashboardPage.getSubmitButtonText(), "Add Task",
            "Submit button should revert to 'Add Task' after cancel");
        Assert.assertFalse(dashboardPage.isCancelButtonDisplayed(),
            "Cancel button should be hidden after cancel");
    }

    @Test(groups = {"ui"},
          description = "Deleting a task with confirmation removes it from the list")
    public void deleteTaskWithConfirmation_removesFromList() {
        // Create a task via API
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"title\":\"Task to delete\"}")
        .when()
            .post("/api/tasks");

        driver.navigate().refresh();

        int beforeCount = dashboardPage.getTaskCount();
        dashboardPage.deleteTask(0);
        int afterCount = dashboardPage.getTaskCount();

        Assert.assertEquals(afterCount, beforeCount - 1,
            "Task count should decrease by 1 after deletion");
    }

    @Test(groups = {"ui"},
          description = "Cancelling the delete modal keeps the task in the list")
    public void cancelDeleteModal_taskRemainsInList() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + authToken)
            .body("{\"title\":\"Task that should survive\"}")
        .when()
            .post("/api/tasks");

        driver.navigate().refresh();

        int beforeCount = dashboardPage.getTaskCount();
        dashboardPage.deleteTaskAndCancel(0);

        Assert.assertFalse(dashboardPage.isDeleteModalDisplayed(),
            "Modal should be closed after cancel");
        Assert.assertEquals(dashboardPage.getTaskCount(), beforeCount,
            "Task count should be unchanged after cancelling delete");
    }
}
