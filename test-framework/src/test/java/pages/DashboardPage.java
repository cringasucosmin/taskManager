package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * DashboardPage — Page Object for dashboard.html.
 *
 * This is the main page after login — shows the task list,
 * filter buttons, and the create task form at the top.
 */
public class DashboardPage extends BasePage {

    // ===== LOCATORS =====
    private final By taskTitleInput     = By.id("task-title");
    private final By taskDescInput      = By.id("task-description");
    private final By taskSubmitBtn      = By.id("task-submit-btn");
    private final By taskCancelBtn      = By.id("task-cancel-btn");
    private final By taskError          = By.id("task-error");
    private final By taskList           = By.id("task-list");
    private final By taskCards          = By.cssSelector(".task-card");
    private final By logoutBtn          = By.id("logout-btn");
    private final By navUsername        = By.id("nav-username");
    private final By deleteModal        = By.id("delete-modal");
    private final By confirmDeleteBtn   = By.id("confirm-delete-btn");
    private final By cancelDeleteBtn    = By.id("cancel-delete-btn");
    private final By emptyState         = By.cssSelector(".empty-state");
    private final By loadingIndicator   = By.cssSelector(".loading");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    // ===== NAVIGATION =====

    public DashboardPage open() {
        navigateTo("/dashboard.html");
        return this;
    }

    // ===== TASK CREATION =====

    /** Fills in and submits the new task form */
    public DashboardPage createTask(String title, String description) {
        type(taskTitleInput, title);
        if (description != null && !description.isEmpty()) {
            type(taskDescInput, description);
        }
        click(taskSubmitBtn);
        waitForTaskListToLoad();
        return this;
    }

    /** Creates a task with title only */
    public DashboardPage createTask(String title) {
        return createTask(title, "");
    }

    /** Submits the task form with an empty title (for negative testing) */
    public DashboardPage submitEmptyTitle() {
        // Clear any existing value and submit
        driver.findElement(taskTitleInput).clear();
        click(taskSubmitBtn);
        return this;
    }

    // ===== TASK EDITING =====

    /** Clicks the Edit button for the task at the given index (0-based) */
    public DashboardPage clickEditOnTask(int index) {
        List<WebElement> editButtons = driver.findElements(By.cssSelector(".edit-btn"));
        editButtons.get(index).click();
        return this;
    }

    /** Updates the title in the edit form and saves */
    public DashboardPage updateTaskTitle(String newTitle) {
        type(taskTitleInput, newTitle);
        click(taskSubmitBtn);
        waitForTaskListToLoad();
        return this;
    }

    /** Clicks Cancel on the edit form */
    public DashboardPage cancelEdit() {
        click(taskCancelBtn);
        return this;
    }

    // ===== TASK DELETION =====

    /** Clicks the Delete button for the task at the given index, then confirms */
    public DashboardPage deleteTask(int index) {
        List<WebElement> deleteButtons = driver.findElements(By.cssSelector(".delete-btn"));
        deleteButtons.get(index).click();
        // Wait for modal to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteModal));
        click(confirmDeleteBtn);
        waitForTaskListToLoad();
        return this;
    }

    /** Clicks Delete but then cancels the confirmation modal */
    public DashboardPage deleteTaskAndCancel(int index) {
        List<WebElement> deleteButtons = driver.findElements(By.cssSelector(".delete-btn"));
        deleteButtons.get(index).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteModal));
        click(cancelDeleteBtn);
        return this;
    }

    // ===== FILTERING =====

    /** Clicks the filter button with the given status label (ALL, PENDING, IN_PROGRESS, DONE) */
    public DashboardPage filterByStatus(String status) {
        By filterBtn = By.cssSelector(".filter-btn[data-status='" + status + "']");
        click(filterBtn);
        waitForTaskListToLoad();
        return this;
    }

    // ===== LOGOUT =====

    public void logout() {
        click(logoutBtn);
    }

    // ===== STATE QUERIES =====

    /** Waits for the loading indicator to disappear, meaning tasks have loaded */
    private void waitForTaskListToLoad() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingIndicator));
        } catch (Exception e) {
            // Loading may be too fast to catch — that's fine
        }
    }

    /** Returns the number of task cards currently visible */
    public int getTaskCount() {
        waitForTaskListToLoad();
        return driver.findElements(taskCards).size();
    }

    /** Returns the title text of the task at the given index */
    public String getTaskTitle(int index) {
        List<WebElement> titles = driver.findElements(By.cssSelector(".task-title"));
        return titles.get(index).getText();
    }

    /** Returns the status badge text of the task at the given index */
    public String getTaskStatus(int index) {
        List<WebElement> badges = driver.findElements(By.cssSelector(".task-status-badge"));
        return badges.get(index).getText();
    }

    /** Returns true if the "no tasks" empty state is shown */
    public boolean isEmptyStateDisplayed() {
        waitForTaskListToLoad();
        return isDisplayed(emptyState);
    }

    /** Returns true if the delete confirmation modal is visible */
    public boolean isDeleteModalDisplayed() {
        return isDisplayed(deleteModal);
    }

    /** Returns the task form error message */
    public String getTaskError() {
        return getText(taskError);
    }

    /** Returns true if the task form error is visible */
    public boolean isTaskErrorDisplayed() {
        return isDisplayed(taskError);
    }

    /** Returns the username shown in the navbar */
    public String getNavUsername() {
        return getText(navUsername);
    }

    /** Returns the text of the submit button (changes to "Save Changes" when editing) */
    public String getSubmitButtonText() {
        return getText(taskSubmitBtn);
    }

    /** Returns true if the Cancel button is visible (i.e., we are in edit mode) */
    public boolean isCancelButtonDisplayed() {
        return isDisplayed(taskCancelBtn);
    }
}
