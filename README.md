# eCommerce UI Automation Framework

[![UI Test Suite](https://github.com/ssgv-b/ecommerce-ui-automation-framework/actions/workflows/ci.yaml/badge.svg?branch=main)](https://github.com/ssgv-b/ecommerce-ui-automation-framework/actions/workflows/ci.yaml)
![Java](https://img.shields.io/badge/Java-21-007396)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36)
![Selenium](https://img.shields.io/badge/Selenium-4.41.0-43B02A)
![TestNG](https://img.shields.io/badge/TestNG-7.12.0-DD0031)
![Allure](https://img.shields.io/badge/Allure-2.35.3-8A2BE2)

Java UI automation framework for [AutomationExercise](https://automationexercise.com), a live e-commerce demo site. Built with Selenium WebDriver 4, TestNG, the Page Object Model and Allure reporting, and run headless in GitHub Actions on every push and pull request to `main`.

The suite covers 25 of the site's 26 [published test cases](https://automationexercise.com/test_cases) plus 5 additional negative and edge cases, 30 tests in total. See [Test coverage](#test-coverage).

## Quick start

Prerequisites: JDK 21, Maven 3.9+, Chrome. Selenium Manager resolves the matching driver automatically.

```bash
mvn clean test
```

```bash
mvn allure:serve
```

The first command runs the full suite headed in local Chrome; the second opens the interactive Allure report.

## Running tests

| Command | What it runs |
| --- | --- |
| `mvn clean test` | Full suite, all groups |
| `mvn clean test -Psmoke` | `smoke` group |
| `mvn clean test -Pregression` | `regression` group |
| `mvn clean test -Pcritical-path` | `critical_path` group |
| `mvn clean test -Pci` | The CI configuration locally: headless, `parallel=classes` with 3 threads, `config-ci.properties` |
| `mvn -DskipTests verify` | Formatting (Spotless, Palantir Java Format) and static analysis (SpotBugs) gates, without the UI suite |

Any configuration key can be overridden on the command line:

```bash
mvn clean test -Dbrowser=firefox -Dheadless=true
```

### Groups

| Group | Meaning |
| --- | --- |
| `smoke` | Fast confidence subset |
| `regression` | Everything |
| `critical_path` | Register, login and order journeys; retried in CI |
| `destructive` / `non_destructive` | Creates and deletes accounts or posts data, vs. read-only |
| `fast` / `slow` | Runtime hint; `slow` tests create an account through the UI |
| `negative` | Error paths and guardrails |
| `download` | Asserts on a browser file download; retried in CI, not supported on a Grid |
| `auth`, `cart`, `checkout`, `order`, `products`, `contact`, `navigation`, `data_integrity` | Domain tags for ad-hoc selection |

## Architecture

```
src/main/java
├── components/   reusable UI parts: NavBar, Footer, category/brand filters, add-to-cart modal
│                 (BaseComponent holds the safe interaction wrappers and waits)
├── pages/        page objects; constructors wait for a page-specific signal element
├── flows/        multi-page business flows (TestFlows) composed from pages
├── models/       immutable test-data value objects with validating builders
├── constants/    config keys, group names, product fixtures
└── framework/
    ├── drivers/     DriverFactory (ThreadLocal<WebDriver>, local + RemoteWebDriver), DriverContext (immutable)
    ├── base/        BasePage, BaseTest (per-test driver lifecycle)
    ├── listeners/   failure screenshot, CI-only retry policy
    ├── testdata/    factories for users, registration profiles and cards
    ├── utils/       ConfigReader, text/price parsing, download polling, shared assertions
    └── exceptions/  FrameworkException → ElementNotFoundException, PageStateException
src/test/java/tests   test classes grouped by domain: checkout, contact, login, misc, orders, products
src/test/resources    config.properties, config-ci.properties, logback-test.xml, allure.properties
testng.xml            suite definition and listener registration
```

### Design decisions

- **Driver lifecycle.** `DriverFactory` owns a `ThreadLocal<WebDriver>`. `BaseTest` creates a fresh driver for every test method and quits it in an `alwaysRun` `@AfterMethod`. The driver and its wait durations travel together as an immutable `DriverContext`, so pages and components never touch static state. The suite is parallel-safe at `parallel=classes`, which is the CI mode.
- **Layers and dependency direction.** Tests express intent through `TestFlows` and page methods. Pages are built from components. `BaseComponent` exposes its wrappers as `protected`, so locators and waits stay inside the page and component layer.
- **Page-load contract.** Every page constructor waits on a page-specific signal element and fails fast if it is absent, so a navigation bug surfaces where the page object is created rather than several steps later. `HomePage` currently exposes `verifyHomePageLoaded()` instead of a constructor wait; unifying this is tracked as ECF-404 in the backlog.
- **Waits.** Explicit waits only; the implicit wait is set to zero. Two timeouts exist: `waitTimeout` for required elements and a short `optionalWaitTimeout` for elements that are usually absent, such as the cookie-consent button, so "maybe present" checks do not burn the full timeout.
- **Resilient interactions.** `click(By)` re-locates and retries once on `ElementClickInterceptedException` or `StaleElementReferenceException`. `enterText` clears, types, and retries once on staleness. Retries log at `WARN` so their frequency stays visible in CI output.
- **Ad blocking at the source.** The demo site serves AdSense overlays that intercept clicks. Chrome is launched with `--host-resolver-rules` that resolve ad and analytics hosts to `NOTFOUND`, so the ad iframes never load. No sleeps and no overlay-dismiss logic are needed.
- **Retry policy.** `RetryTransformer` attaches `RetryAnalyzer` only in CI (`test.env=ci`, `CI=true` or `GITHUB_ACTIONS=true`) and only to `critical_path` and `download` tests; `@NoRetry` opts a test out. Nothing retries locally, so flakiness is visible where it can be fixed.
- **Test data.** Every test gets a unique user built by factories with a full UUID suffix, so parallel runs and retries cannot collide. Destructive tests delete the account they created, with a `finally` safety net.
- **Typed failures.** UI lookup and state failures throw `ElementNotFoundException` or `PageStateException` with a descriptive message instead of a bare `RuntimeException`.
- **Quality gates.** Spotless (Palantir Java Format) and SpotBugs are bound to the `verify` phase; run `mvn -DskipTests verify` before pushing.

## Configuration

Every key resolves in this order: JVM system property, then environment variable (`baseUrl` becomes `BASE_URL`), then the properties file. The file is `config.properties` by default, or `config-<env>.properties` when `test.env` or `TEST_ENV` is set; the `ci` profile sets `test.env=ci`.

| Key | Local default | CI (`config-ci.properties`) | Purpose |
| --- | --- | --- | --- |
| `baseUrl` | `https://automationexercise.com` | same | Target site |
| `browser` | `chrome` | `chrome` | `chrome` or `firefox` |
| `headless` | `false` | `true` | Headless mode (`--headless=new` on Chrome) |
| `waitTimeout` | `5` | `10` | Explicit wait for required elements, seconds |
| `optionalWaitTimeout` | `2` | `3` | Wait for usually-absent elements, seconds |
| `pageLoadTimeout` | `30` | `45` | Page load timeout, seconds |
| `maxRetryCount` | `1` | `2` | Retries per eligible test; only applied in CI |
| `downloadDir` | `downloads` | `target/downloads` | Browser download directory |
| `remote` | `false` | not set | Route through `RemoteWebDriver` |
| `remoteUrl` | `http://localhost:4444` | not set | Selenium Grid URL |
| `chromeBinary` | not set | set by the workflow | Path to a pinned Chrome binary |

## Continuous integration

[`.github/workflows/ci.yaml`](.github/workflows/ci.yaml) runs on every push and pull request to `main`:

1. JDK 21 (Temurin) with a Maven dependency cache.
2. Chrome 141 pinned through `browser-actions/setup-chrome` together with the matching ChromeDriver, passed to Selenium via `chromeBinary` and `webdriver.chrome.driver`, so Selenium Manager cannot drift to a mismatched version.
3. `mvn clean test -Pci`: headless, `parallel=classes`, 3 threads, `config-ci.properties`.
4. `target/allure-results` uploaded as a build artifact, on failure as well.

## Reporting

- `mvn allure:serve` opens the interactive report; `mvn allure:report` writes a static one under `target/site/allure-maven-plugin`.
- Raw results are written to `target/allure-results`.
- `TestListeners` attaches a screenshot to every failed test.
- Logging goes through SLF4J and Logback (`logback-test.xml`): the framework packages log at `DEBUG` for wait tracing and retry warnings, the root logger at `WARN`.

## Remote execution (Selenium Grid)

The suite runs unchanged against a remote Selenium Grid; the switch is config-only. `remote` defaults to `false`, so nothing changes unless you opt in.

Start a local single-browser Grid with Docker, then point the suite at it:

```bash
docker run -d -p 4444:4444 -p 7900:7900 --shm-size=2g --name selenium-chrome selenium/standalone-chrome:4.41.0
```

```bash
mvn clean test -Dremote=true -DremoteUrl=http://localhost:4444
```

Watch the browser live at `http://localhost:7900` (password `secret`). Tear down with `docker stop selenium-chrome && docker rm selenium-chrome`.

On Apple Silicon there is no `selenium/standalone-chrome` arm64 image; use `selenium/standalone-chromium` instead. No code change is needed.

## Test coverage

Mapping to the site's published test cases:

| # | Site test case | Test |
| --- | --- | --- |
| 1 | Register User | `LoginTests.registerUserAndDelete` |
| 2 | Login User with correct email and password | `LoginTests.createAccountLoginAndDelete` |
| 3 | Login User with incorrect email and password | `LoginTests.loginWithInvalidCredentials` |
| 4 | Logout User | `LoginTests.createAccountAndLogOut` |
| 5 | Register User with existing email | `LoginTests.registerUserWithExistingEmail` |
| 6 | Contact Us Form | `ContactUsTests.submitContactUsForm` (file-upload step not yet covered) |
| 7 | Verify Test Cases Page | `MiscellaneousTests.verifyTestCases` |
| 8 | Verify All Products and product detail page | `ProductTests.userCanViewProductDetailsFromProductsPage` |
| 9 | Search Product | `ProductTests.userCanSearchForProductByName` |
| 10 | Verify Subscription in home page | `MiscellaneousTests.verifySubscriptionInHomePage` |
| 11 | Verify Subscription in Cart page | `MiscellaneousTests.verifySubscriptionInCartPage` |
| 12 | Add Products in Cart | `ProductTests.userCanAddMultipleProductsToCart` |
| 13 | Verify Product quantity in Cart | `ProductTests.userCanAddMultipleQuantitiesOfProductToCart` |
| 14 | Place Order: Register while Checkout | `PlaceOrderTests.userCanPlaceOrderImmediatelyAfterRegistration` |
| 15 | Place Order: Register before Checkout | `PlaceOrderTests.userCanPlaceOrderAfterRegisteringAndAddingProducts` |
| 16 | Place Order: Login before Checkout | `PlaceOrderTests.userCanPlaceAnOrderAfterLoginToExistingAccount` |
| 17 | Remove Products From Cart | `ProductTests.userCanAddAndRemoveProductsFromCart` |
| 18 | View Category Products | `ProductTests.userCanBrowseProductsByCategory` |
| 19 | View & Cart Brand Products | `ProductTests.userCanFilterProductsByBrand` |
| 20 | Search Products and Verify Cart After Login | `ProductTests.userCartIsPreservedAfterLogin` |
| 21 | Add review on product | `ProductTests.userCanAddReviewToProduct` |
| 22 | Add to cart from Recommended items | `ProductTests.userCanAddRecommendedProductToCart` |
| 23 | Verify address details in checkout page | `CheckoutTests.verifyAddressDetailsDuringCheckout` |
| 24 | Download Invoice after purchase order | `PlaceOrderTests.userCanDownloadInvoiceAfterPlacingOrder` |
| 25 | Verify Scroll Up using 'Arrow' button and Scroll Down functionality | `MiscellaneousTests.verifyScrollUpFunctionality` |
| 26 | Verify Scroll Up without 'Arrow' button and Scroll Down functionality | not yet covered (ECF-412) |

Additional cases beyond the site's list:

| Test | Checks |
| --- | --- |
| `CheckoutTests.verifyCheckoutIsNotAvailableOnEmptyCart` | The checkout control is hidden on an empty cart |
| `CheckoutTests.verifyGuestUserCannotGoToCheckout` | A guest is sent to login/register instead of checkout |
| `ProductTests.verifyNoProductsAreReturnedWhenSearchingNonsenseTerm` | A nonsense search returns zero product cards |
| `MiscellaneousTests.verifyInvalidEmailIsNotAllowedForNewsletter` | A malformed newsletter email fails HTML5 validation |
| `MiscellaneousTests.verify404PageIsServedOnInvalidUrl` | An unknown route serves the 404 page |

## Known limitations

- Tests run against the live AutomationExercise site and can fail on site-side instability or content changes, independent of code correctness. CI retries `critical_path` and `download` tests up to `maxRetryCount` times for that reason.
- Accounts are provisioned through the UI. An API-based data lifecycle is planned once the REST Assured work lands (ECF-403).
- Ad blocking is Chrome-only, so `-Dbrowser=firefox` runs may still hit ad overlays that intercept clicks (ECF-413).
- Browser file downloads differ by OS and browser, and are not supported on a Grid: the file lands on the node's disk, not the runner's. Run download scenarios locally.
- Test case 6's file-upload step and test case 26 are not covered yet.

## Roadmap

Open and completed work is tracked ticket by ticket in [docs/BACKLOG.md](docs/BACKLOG.md).
