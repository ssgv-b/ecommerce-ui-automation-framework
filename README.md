# eCommerce UI Automation Framework

![Java](https://img.shields.io/badge/Java-21-007396)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36)
![Selenium](https://img.shields.io/badge/Selenium-4.41.0-43B02A)
![TestNG](https://img.shields.io/badge/TestNG-7.12.0-DD0031)
![Allure](https://img.shields.io/badge/Allure-2.24.0-8A2BE2)

Java UI automation framework for [AutomationExercise](https://automationexercise.com) built with Selenium WebDriver, TestNG, Page Object Model, and Allure reporting.

## Quick Start

1. Install Java 21 and Maven 3.9+.
2. Run tests:

```bash
mvn clean test
```

3. Open Allure report:

```bash
mvn allure:serve
```

## Tech Stack

- Java 21
- Maven
- Selenium WebDriver
- TestNG
- Allure

## Project Structure

- `src/main/java` framework and reusable test code
- `src/main/java/components` reusable UI components
- `src/main/java/pages` page objects
- `src/main/java/flows` reusable business flows
- `src/main/java/framework` drivers, config, listeners, utilities
- `src/main/java/models` test data models
- `src/test/java/tests` test classes grouped by domain
- `src/test/resources` config and test resources
- `testng.xml` suite and listeners

## Design Notes

- Page constructors validate that the page is loaded by waiting for a unique page signal element.
- This is intentional fail-fast behavior so navigation issues are detected at page object creation time.
- Base flows and tests rely on these constructor assertions instead of adding redundant page checks in each test.

## Prerequisites

- JDK 21
- Maven 3.9+
- Chrome and/or Firefox installed

## Run Tests

Run full suite:

```bash
mvn clean test
```

Run smoke profile:

```bash
mvn clean test -Psmoke
```

Run regression profile:

```bash
mvn clean test -Pregression
```

Run critical path profile:

```bash
mvn clean test -Pcritical-path
```

Run CI profile locally (headless + parallel):

```bash
mvn clean test -Pci
```

## Allure Reporting

Generate static report:

```bash
mvn allure:report
```

Open interactive report:

```bash
mvn allure:serve
```

Allure results directory:

- `target/allure-results`

## Configuration

- Default config file is loaded from `src/test/resources/config.properties`.
- Environment-specific config is resolved by `test.env` / `TEST_ENV` (example: `config-ci.properties`).
- Any key can be overridden by JVM system property or environment variable.
- Example override:

```bash
mvn clean test -Dbrowser=firefox -Dheadless=true
```

## Known Limitations

- Tests depend on the live AutomationExercise environment and can fail on site-side instability or content changes.
- Some tests rely on seeded credentials/test data being valid in the target environment.
- Browser file-download behavior differs by OS/browser and may require local driver/browser tuning.
- The suite currently focuses on UI flow validation; there is no API-level mocking layer.

## Notes

- Test execution is configured through `testng.xml` and Maven profiles.
- Retry behavior is managed via TestNG listeners/transformers.
- Failure screenshots are attached in Allure reports.
