# Task Manager — QA Automation Portfolio

![QA Tests](https://github.com/cringasucosmin/taskManager/actions/workflows/test.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange)
![Selenium](https://img.shields.io/badge/Selenium-4.18-green)
![TestNG](https://img.shields.io/badge/TestNG-7.9-blue)
![RestAssured](https://img.shields.io/badge/RestAssured-5.4-yellow)

A complete QA automation portfolio project built from scratch. Includes a Spring Boot web application and a full test automation framework that tests it — with UI tests, API tests, CI/CD pipeline, and HTML test reports.

---

## Project Structure

```
taskManager/
├── src/                          ← Spring Boot application (app under test)
│   └── main/java/com/taskmanager/
│       ├── controller/           ← REST endpoints (Auth + Tasks)
│       ├── service/              ← Business logic
│       ├── model/                ← User, Task, TaskStatus
│       └── resources/static/    ← Frontend (HTML/CSS/JS)
│
├── test-framework/               ← Selenium + TestNG automation framework
│   └── src/test/java/
│       ├── base/                 ← BaseTest, BaseApiTest
│       ├── pages/                ← Page Object Model (LoginPage, DashboardPage)
│       ├── tests/
│       │   ├── ui/               ← LoginTests, TaskCRUDTests, TaskFilterTests
│       │   └── api/              ← AuthApiTests, TasksApiTests
│       └── utils/                ← ConfigReader, ExtentReportListener, ScreenshotUtils
│
└── .github/workflows/test.yml   ← CI/CD pipeline
```

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Build | Maven | 3.9 |
| App Framework | Spring Boot | 3.2 |
| Database | H2 (in-memory) | — |
| UI Testing | Selenium WebDriver | 4.18 |
| Test Runner | TestNG | 7.9 |
| Test Design Pattern | Page Object Model (POM) | — |
| API Testing | RestAssured | 5.4 |
| Test Reports | Extent Reports | 5.1 |
| CI/CD | GitHub Actions | — |

---

## Test Coverage

| Suite | Tests | Type |
|-------|-------|------|
| AuthApiTests | 6 | API |
| TasksApiTests | 7 | API |
| LoginTests | 5 | UI |
| TaskCRUDTests | 6 | UI |
| TaskFilterTests | 5 | UI |
| **Total** | **29** | — |

All 29 tests pass locally and in CI.

---

## How to Run

### Prerequisites
- Java 17+
- Maven 3.9+
- Google Chrome (for UI tests)

### 1. Start the application

```bash
cd taskManager
mvn spring-boot:run
```

App starts at `http://localhost:8080`

### 2. Run all tests

```bash
cd test-framework
mvn test -Dheadless=true
```

### 3. Run only API tests (no browser needed)

```bash
cd test-framework
mvn test -Dtest=tests.api.AuthApiTests,tests.api.TasksApiTests
```

### 4. View the test report

Open `test-framework/test-output/ExtentReport.html` in any browser.

---

## CI/CD Pipeline

Every push to `main` triggers the GitHub Actions workflow automatically:

1. Checkout code
2. Set up Java 17
3. Build Spring Boot app
4. Start app in background on port 8080
5. Wait for health check
6. Run all 29 tests in headless Chrome
7. Upload ExtentReport.html as downloadable artifact

See [.github/workflows/test.yml](.github/workflows/test.yml)

---

## Application Features

The Task Manager app provides:
- **User registration and login** (JWT-based auth)
- **Create, read, update, delete tasks**
- **Filter tasks** by status (PENDING / IN_PROGRESS / DONE)
- REST API + web UI

Default test credentials: `qauser` / `qapass123` (created via API before UI tests run)

---

## Author

**Andrei-Cosmin Cringasu**
Junior QA Engineer — Bucharest, Romania
[github.com/cringasucosmin](https://github.com/cringasucosmin)
