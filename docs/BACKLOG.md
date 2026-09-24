# Framework Backlog

Prioritized implementation backlog from the Lead-QA framework review.
One ticket per branch. Priority lives in the **section**, not the ticket number: `ECF-1xx/2xx/3xx` are the original P1/P2/P3 tickets; `ECF-4xx` were added by the 2026-09-19 full review and are filed under whichever priority section fits.
API-based test-data seeding (**ECF-403**) is intentionally parked until the REST Assured course is done — see *Deferred* at the bottom; tickets that it would unblock say so.

Legend: ✅ done · 🔶 in progress · ⬜ not started · ⏸ deferred

---

## P0 — Critical (stability) — ✅ DONE

- ✅ **Ad / overlay blocking** — implemented via Chrome host-resolver rules blocking known AdSense URLs. Ad iframes no longer load, so click interception from ads is eliminated at the source.
- ✅ **Resilient click rework** — `click(By)` now re-locates the element via `waitAndScrollToElement(locator)` on `ElementClickInterceptedException` (no more stale-reference reuse). Added a scoped `click(WebElement)` overload that waits for clickability.
- ✅ **Raw click / findElement holes closed** — interactions in `ProductsPage`, `CategoryFilterComponent`, `BrandFilterComponent`, `AddToCartModalComponent`, and `CheckoutPage` now route through the safe wrappers / scoped overload instead of raw `.click()` / `.findElement()`.
  - ⚠️ **Correction (2026-09-19 review):** six raw `.click()` calls are still in the tree — `AddToCartModalComponent.continueShopping` / `goToCart`, `HomePage.addRecommendedProductToCart` (carousel), `CartPage.clearCart` (remove button), `ProductsPage.viewSingleProduct`, `BasePage.scrollToTopArrow` — plus two raw `driver.findElement` (`AddToCartModalComponent.scopeToModal`, `ContactPage.uploadFile`). Ad-blocking is also Chrome-only, so Firefox still gets the overlays. Tracked in **ECF-406** (clicks/locators) and **ECF-413** (Firefox).

---

## P1 — High

### ECF-101 · SLF4J logging binding — ✅ DONE
**Branch:** `fix/slf4j-binding` (folded into the ad-level refactor commit)
**Problem:** Code logs via SLF4J everywhere but originally had no binding, so log lines were silently discarded.
**Delivered:**
- Added `slf4j-api` + `logback-classic` (test scope) to `pom.xml`.
- Added `src/test/resources/logback-test.xml` — console appender, root `WARN`, DEBUG loggers on the real packages: `components`, `flows`, `pages`, `framework`.
- `BaseComponent` uses `protected final Logger log = LoggerFactory.getLogger(getClass())` (names the logger after the concrete subclass).
- `DriverFactory` uses `LoggerFactory.getLogger(DriverFactory.class)` (static utility — correct, compiles).

**Follow-up nits (optional, not blocking):** `logback-test.xml` has no trailing newline; nothing you own logs at `INFO`, so `WARN` root only surfaces retry/quit warnings while the DEBUG package loggers light up the wait tracing — intended.

### ECF-102 · Remove hard dependency on shared seeded account — ✅ DONE
**Branch:** `refactor/seeded-user-independence`
**Problem:** `existingSeededUser` (`existing@user.com`) is a hardcoded account on a public, shared site; anyone can delete it and `destructive` tests delete accounts. One collision reds the whole group.
**Delivered:**
- Added `TestFlows.registerAndLogOut(TestUser)` — provisions a fresh account and returns to the logged-out login page, so login-to-existing scenarios run against a self-owned account.
- Transformed all 4 seeded-user tests to self-provision the per-method unique `testUser` and delete it (try/finally + `AccountCleanupHelper`):
  - `PlaceOrderTests.userCanPlaceAnOrderAfterLoginToExistingAccount`
  - `ProductTests.userCartIsPreservedAfterLogin`
  - `LoginTests.registerUserWithExistingEmail`
- Removed `LoginTests.logInAccountAndLogOut` (redundant with `createAccountLoginAndDelete` once self-provisioned).
- Fixed `LoginTests.createAccountAndLogOut` orphan leak — now logs back in and deletes the account.
- Removed `UserIdentityDataFactory.existingSeededUser()` and the now-dead `TestFlows.loginAsExistingUser()`.
- Reclassified affected tests from `non_destructive`/`fast` → `destructive`/`slow` (they now create + delete accounts).
- Verified: `LoginTests` (5/5) + both critical-path transforms pass headless against the live site.

**Known limitation (accepted):** If a test fails *after* provisioning but *before* it logs in, the fresh account can orphan (the finally-cleanup needs a logged-in `HomePage`). Unique emails prevent collisions, so this is tolerable; a future identity-based cleanup (log in by identity, then delete) would close it — revisit alongside the API work (**ECF-403**).

### ECF-103 · Fix unique-suffix collision risk — ✅ DONE
**Branch:** `fix/expand-unique-user-suffix` (merged, PR #7)
**Problem:** `generateUniqueSuffix()` truncates a UUID to 5 hex chars (~1M space); birthday collisions under `parallel=classes` + retries surface as spurious "Email already exists".
**Delivered:** `generateUniqueSuffix()` now returns the full UUID (`UUID.randomUUID().toString().replace("-","")`) — no truncation, so the collision surface is eliminated by construction rather than probabilistically narrowed.
**Note:** No stress-run artifact was committed; the full-UUID space makes the "zero duplicate-email failures" bar hold by construction.

### ECF-104 · Decouple tests from brittle catalog data — ✅ DONE
**Branch:** `refactor/externalize-product-expectations-and-details`
**Problem:** `ProductTests` asserts exact literals (`Rs. 400`, `H&M`, `Men > Tshirts`); catalog changes fail tests for unrelated reasons.
**Delivered:**
- Added `constants/Product` enum — 34 products with name + price; `MEN_TSHIRT` carries the full 6-field detail via a chained constructor; all fields `final`.
- Added `ProductTextParser.parsePrice` (`String → BigDecimal`; strips the `Rs.` prefix — **not** currency-agnostic as originally written here, corrected 2026-09-19); reused in `CartPage` and `ProductDetailsPage`, removing duplicated price stripping.
- `ProductDetails` and `CartItem` model prices as `BigDecimal`.
- Fixed `CartPage.readCartItems` to scope every field read to its row (rows 2+ were picking up row 1's price/quantity/total).
- Split cart-row lookup into a waiting variant (`getCurrentCartItems`, for readers that expect rows) and a non-waiting snapshot (`getCartItems` → `findElements`, for `clearCart`) so clearing no longer burns the wait timeout on the empty check.
- Prices compared with `compareTo(...) == 0` (scale-insensitive) with a value-carrying failure message.
- Removed dead `addToCartButtonOverlay` locator; `.gitignore` ignores `CLAUDE.md`.

**Deferred to their own branches:**
- Split categories/brands into a separate enum (still magic strings in `ProductTests`).
- Remaining `ProductTests` reworks.
- `Optional` for the nullable detail getters — nulls are load-bearing but contained to non-`MEN_TSHIRT` constants; promote the four detail fields into a grouped optional bundle if/when a second product needs full detail.
- **Review follow-ups (2026-09-19) → ECF-410:** 31 of the 34 `Product` constants are unused (the enum relocated the literals rather than decoupling from them), and `userCanAddMultipleProductsToCart` still assumes the first two cards on `/products` are Blue Top / Men Tshirt (positional coupling).

### ECF-105 · Fix `enterText` clear semantics + remove duplicate — ✅ DONE
**Branch:** `fix/entertext-clear-semantics`
**Problem:** `enterText()` and `enterTextNoClearing()` had identical bodies (neither cleared). Name implies clear-then-type; edit / re-search flows would append.
**Delivered:**
- `enterText()` now calls `element.clear()` before `sendKeys(text)` — the name finally matches the behavior.
- Audited all 28 `enterText` call sites (payment, registration, contact, login/signup, review, quantity, order comment, search): every one is empty-field-entry semantics, so clearing is safe everywhere and actively correct for `ProductsPage` search (repeated searches no longer concatenate).
- `enterTextNoClearing()` had exactly one caller (`Footer.enterFooterEmailAndSubscribe`, also an empty field) — repointed it to `enterText` and deleted the redundant method. Zero lingering references; compiles clean.
**Done when:** ✅ `enterText` clears; the redundant twin is gone.

### ECF-401 · Enforce the quality gates for real — ⬜
**Branch:** `ci/enforce-quality-gates`
**Problem (2026-09-19 review):** ECF-301 bound `spotless:check` + `spotbugs:check` to `verify`, but nothing ever runs `verify`: CI runs `mvn clean test` (stops one phase short), and locally `mvn -DskipTests verify` crashes inside Spotless with `NoSuchMethodError` because Maven runs on the Homebrew JDK 25 while `java` on PATH is 21, and palantir-java-format 2.50.0 predates JDK 25. SpotBugs itself passes (0 bugs). The gates are effectively dead on both ends — a formatting violation cannot fail a PR today, which undercuts the "gates are green" claim.
**Scope:**
- Separate, fast CI job (`lint`) running `mvn -DskipTests verify` before the UI job; the UI job `needs: lint`.
- Upgrade `palantir-java-format` to a release that supports JDK 25 (or pin Maven to JDK 21 via `JAVA_HOME` / Maven toolchains) — verify with `mvn spotless:check` on this machine.
- `maven-enforcer-plugin` with `requireJavaVersion` + `requireMavenVersion`, so a wrong JDK fails with a readable message instead of a javac-internals stack trace.
- Drop the dead `ci`-profile properties in `pom.xml` (`waitTimeout`, `headless`, `browser`, `downloadDir`, `pageLoadTimeout`) — they stopped reaching the JVM in ECF-308 and the `waitTimeout=8` there contradicts the `10` in `config-ci.properties`.
- `mvn -DskipTests verify` is already documented as the pre-push command in the README (ECF-409).
**Done when:** A deliberate formatting violation on a branch turns the PR check red; `mvn -DskipTests verify` passes locally on the pinned JDK; no dead profile properties remain.

### ECF-402 · Allure report depth — ⬜
**Branch:** `feat/allure-report-depth`
**Problem:** Allure is advertised in the README badge, but the codebase has zero `@Step`, `@Epic`, `@Feature`, `@Story`, `@Severity` or `@Description` annotations. The report is a flat list of method names plus a failure screenshot — no URL, page source or browser console on failure — and the Behaviors tab is empty. This is the artefact an interviewer opens first. The AspectJ weaver is already wired (it serves `@Attachment`), so `@Step` works with no build change.
**Scope:**
- `@Epic` / `@Feature` / `@Story` per test class (e.g. Epic "AutomationExercise UI", Feature "Checkout", Story per scenario); `@Severity` + `@Description` per test; `@TmsLink` / `@Link` to the site's numbered test case where one exists (the README coverage table has the mapping).
- `@Step` on `TestFlows` methods and on page-object public actions — step text reads as a user action ("Add {count} products to cart"), not as a method name.
- On failure, attach alongside the screenshot: current URL, page source (`text/html`), browser console logs (Chrome `LogType.BROWSER`), and — once ECF-304 lands — the DataFaker seed as an Allure parameter.
- `src/test/resources/environment.properties` (browser, headless, baseUrl, test.env) + `categories.json` (e.g. "Live-site instability" for `TimeoutException` / `WebDriverException`, "Product defect" for `AssertionError`).
- Retire `TestExecutionContext` + its `ThreadLocal` in `TestListeners` — it stores a method name already on `ITestResult` and sets result attributes that nothing reads. The listener keeps the screenshot + the new attachments only.
**Done when:** Behaviors tab is populated for every test; a failed test shows steps, screenshot, URL, page source and console; `TestExecutionContext` is gone.

### ECF-404 · Page-load contract consistency — ⬜
**Branch:** `refactor/page-load-contract`
**Problem:** README and CLAUDE.md state that every page constructor waits on a unique page-signal element. `HomePage` is the exception: its constructor does not wait, `verifyHomePageLoaded()` is called from only two places (`TestFlows.openHomePage`, `TestAssertions.deleteAccountAndAssertHomePage`), and six methods hand back an unverified `HomePage` (`NavBar.navigateToHome`, `AccountCreatedPage` / `AccountDeletedPage.continueToHomePage`, `LoginPage.logInAccount`, `OrderPlacedPage.continueShopping`, `ContactPage.continueToHome`). Root cause: `LoginPage.logInAccount` returns `HomePage` on the *failure* path too, and `loginWithInvalidCredentials` relies on that — so `HomePage` cannot get a constructor wait without breaking it. Separately, two signals are not unique: `ProductsPage` waits on `.product-image-wrapper` (also rendered on Home) and `TestCasesPage` on the generic `h2.title.text-center` (also on Products) — the previous page satisfies the "next page loaded" check.
**Scope:**
- Split the login action: a success-path method returning `HomePage` and a failure-path method that stays on `LoginPage` (e.g. `logInExpectingError`), so the return type never lies.
- Move the `#slider` presence wait + cookie handling into the `HomePage` constructor; delete `verifyHomePageLoaded()` and its two call sites.
- Unique signals: `ProductsPage` → something only `/products` renders (the "All Products" / "Searched Products" title, or `#search_product`); `TestCasesPage` → the "Test Cases" heading text or the URL.
- Watch ECF-307's flake against the new constructor wait (same signal, earlier in the flow).
**Done when:** Every page constructor waits on a signal the previous page cannot satisfy; `logInAccount` no longer returns `HomePage` on failure; the README's design note is true without exceptions.

### ECF-405 · Group hygiene + retry-policy consistency — ⬜ (small)
**Branch:** `chore/group-constants`
**Problem:** Groups are string literals in `@Test`. `non-destructive` (hyphen, 2×) coexists with `non_destructive` (underscore, 10×), so a `non_destructive` filter silently drops two tests. `ProductTests.verifyNoProductsAreReturnedWhenSearchingNonsenseTerm` has **no groups**, so `-Psmoke` / `-Pregression` never run it. `constants.Groups` holds 2 of the 18 names in use. `LoginTests.registerUserAndDelete` sets `retryAnalyzer = RetryAnalyzer.class` directly, bypassing the CI-only / critical-path policy that `RetryTransformer` encodes (it retries locally too).
**Scope:**
- Every group name as a `Groups` constant; tests reference `Groups.X` only. Fix the hyphen typo; group the ungrouped test (`regression`, `products`, `negative`, `non_destructive`, `fast`).
- Remove the explicit `retryAnalyzer` (or, if that test genuinely needs local retries, say why in a comment and tag it `critical_path`).
- Tag download-asserting tests so they skip under `remote=true` (carry-over from ECF-202).
**Done when:** `grep -rn 'groups = {"' src/test` returns nothing; every test has ≥1 group; the transformer is the only retry wiring.

---

## P2 — Medium

### ECF-201 · Stand up CI pipeline — ✅ DONE (core)
**Branch:** `ci/github-actions`
**Delivered:** `.github/workflows/ci.yaml` (on `origin/main`) runs `mvn clean test -Pci` headless on push/PR to main, JDK 21 (temurin) + Maven cache, and uploads `target/allure-results` as an artifact with `if: "!cancelled()"`. PRs get a green/red check.
**Remaining polish — now ticketed (2026-09-19):** rendered/published report, `timeout-minutes`, `concurrency`, nightly run → **ECF-408**; Chrome + Firefox matrix → **ECF-413** (blocked on Firefox ad-blocking). Failure screenshots stay inside allure-results, which the published report will surface.

### ECF-202 · Add RemoteWebDriver / Grid support — ✅ DONE
**Branch:** `feat/remote-driver`
**Problem:** `DriverFactory` only builds local drivers; can't scale or stabilize CI browser versions.
**Delivered:**
- Split options-building from driver construction: `buildChromeOptions` / `buildFirefoxOptions` return configured options; the local `createChrome/FirefoxDriver` methods consume them. Local behavior is unchanged.
- `createDriver` forks on a `remote` flag (default `false`): remote hands the same `Capabilities` to `RemoteWebDriver(gridUrl, options)`; one branch serves both browsers since `ChromeOptions`/`FirefoxOptions` are both `Capabilities`. Local stays the default.
- Added `remote` and `remoteUrl` (default `http://localhost:4444`) config keys. `toGridURL()` wraps the checked `MalformedURLException` into `RuntimeException`, matching existing policy, and is only called on the remote path.
- Ad-blocking host-rules, download prefs, headless, and stability flags travel inside the options, so they apply remotely too. `maximize()` guarded by `!headless` on both paths.
- **Verified** against a local `selenium/standalone-*` Docker Grid via `-Dremote=true` — suite runs green by config only.

**Notes / known limitations (accepted):**
- **Downloads don't work on Grid** — the browser downloads to the *node's* disk, not the runner's, so the file-download test times out under `-Dremote=true`. Expected; downloads were explicitly scoped out of remote. Follow-up: tag download-asserting tests so they're skipped when `remote=true` (removes the one red from remote runs) — folded into **ECF-405**.
- **Apple Silicon (ARM64):** `selenium/standalone-chrome` has no arm64 image; use `selenium/standalone-chromium` locally. Chromium is Chrome-compatible for these options, so no code change needed.

**Follow-ups (own tickets):** Chrome + Firefox matrix via hub + node (docker-compose) → **ECF-413**; CI doesn't provision a Grid yet (`config-ci.properties` stays local).

### ECF-203 · Pin browser versions in CI — ✅ DONE
**Branch:** `ci/pin-browser-versions` (+ follow-ups `ci/Pin-Chrome-Version`, `test/interaction-flakiness-hardening`)
**Problem:** Selenium Manager auto-resolution causes version drift in CI.
**Delivered:**
- `setup-chrome@v1` pins Chrome **141** (Chrome-for-Testing) + `install-chromedriver` pins the matching driver; workflow verifies the installed version.
- Pinning the browser alone wasn't enough — Selenium Manager still auto-resolved a mismatched driver against the runner's default Chrome (150). Fixed by feeding Selenium the pinned binary (`chromeBinary` → `ChromeOptions.setBinary`) and the pinned driver (`-Dwebdriver.chrome.driver`), so it launches 141/141.
- Config single-source fix (see ECF-308) removed a pom override that was silently forcing the old value, so `config-ci` actually takes effect.
**Done when:** ✅ CI logs launch a fixed, intentional Chrome 141 with a matching 141 driver; no `session not created` mismatch, no CDP-version warning.

### ECF-204 · Revisit wait timeouts — ✅ DONE
**Branch:** `tune/wait-timeouts`
**Problem:** 5s local / 8s CI is tight for this target; re-measure now that ads are blocked. Bigger issue found: `acceptCookiesIfPresent` used the **global** wait to look for a consent button that's usually absent, so every HomePage load burned the full timeout then swallowed the exception — a per-test tax across nearly the whole suite.
**Delivered:**
- Added `optionalWaitTimeout` (2s local / 3s CI), threaded through the same path as `waitDuration` (config → `DriverFactory` → `DriverContext` → `BaseComponent`; `BaseComponent` stays config-free). `DriverContext` gained the second `Duration` (still an immutable value object).
- `BaseComponent.waitForOptionalElement(By)` — reusable helper on a dedicated `final optionalWait`, returns `Optional<WebElement>`, swallows `TimeoutException`. `acceptCookiesIfPresent` collapses to one line routing the click through the safe `click(WebElement)` overload via `this::click` (not raw `WebElement::click`, which would bypass the clickability wait).
- Bumped global `waitTimeout` 8→10 in CI as cheap flake insurance (early-return makes a higher cap free on passing tests); local stays 5.
**Verification:** ran the full suite headless **3× back-to-back locally**. Each run had one failure, but all three were *different* tests and all pre-existing live-site flakes (network `ERR_INTERNET_DISCONNECTED`, `StaleElementReferenceException`, CDP `Node ... does not belong to the document`) — **zero `TimeoutException` across all three runs**, confirming the tighter optional-wait and new timeouts introduce no timeout regressions. Cookie optional-wait fired 30–33×/run with no failures.

### ECF-308 · Suite flakiness on live-site interactions — ✅ DONE (partial, evidence-scoped)
**Branch:** `test/interaction-flakiness-hardening`
**Problem:** Surfaced by the ECF-204 3× run — one flake per run, all different tests, none timeout-related: `StaleElementReferenceException` (at `click`, via `NavBar.navigateToCart`), CDP `Node with given id does not belong to the document` (inside a visibility wait in `CreateAccountPage`'s constructor), and a transient network drop.
**Delivered (scoped to the *observed* failures, not blanket):**
- `click(By)` — widened the existing catch to also cover `StaleElementReferenceException` (same remedy as the interception path: re-locate via `waitAndScrollToElement`, retry). This was the actual `click` failure.
- `enterText` — routed through a new `retryOnStale(By, Consumer<WebElement>)` helper that re-locates and retries **once** on staleness, logging the retry (`log.warn`) so future CI runs surface how often it fires instead of silently self-healing.
- Deliberately **not** hardened: `selectByVisibleText`/`selectByValue` (left on their original `waitForClickable`) and `getTextWhenVisible` — neither was an observed failure, and a generic wrapper would have downgraded selects' `enabled` wait to visible-only. Evidence-scoped over speculative.
- Rode-along config fix: trimmed surefire `<systemPropertyVariables>` to just `test.env`, so `config.properties`/`config-*.properties` are the single source of truth. This is what finally makes **ECF-204's `waitTimeout=10` actually take effect in CI** — the pom's profile value was silently overriding the file via system property. (Local `-D` overrides still work.)
**Verification:** compiles; interaction hardening is sound by construction (re-locate + retry). The earlier 3× `-Pci` green run *predates* the stale-retry code, so it validates the config fix + no-regression, not the retry itself — staleness is intermittent and can't be deterministically reproduced. The added retry logging is the ongoing signal.
**Still open (deferred, own follow-up if it recurs):** the CDP `Node...` failure inside `CreateAccountPage`'s constructor wait is a *different* shape (detach during the `ExpectedConditions` poll, which doesn't ignore `WebDriverException`), not a located-then-stale race — a re-locate-after-return retry wouldn't catch it. Leave it to `maxRetryCount`; add a targeted `.ignoring(...)` on the page-signal wait only if it recurs.
**Leftover (2026-09-19):** the now-dead `ci`-profile properties in `pom.xml` that this fix orphaned are removed in **ECF-401**.

### ECF-205 · Repo hygiene cleanup — ✅ DONE
**Branch:** folded into `chore/static-analysis` (ECF-301) — too small for its own PR.
**Problem:** Tracked build output (`allure-report/`, multiple `allure-results/`, `downloads/invoice.txt`) and `.DS_Store` files pollute the tree even though gitignored.
**Delivered:** Most of the problem statement was already stale — `.gitignore` covers `/target/` (CI `target/downloads`), `/allure-report/`, `/allure-results/`, `/.allure/`, `/downloads/`, and `.DS_Store` (both root and `**/`), and none of those were tracked. The only genuinely tracked artifact was `src/test/resources/downloads/invoice.txt` — a stale 59-byte captured invoice referenced by no test (the download test reads `downloadDir`, i.e. `downloads/` / `target/downloads/`, never the resources dir). It slipped the root-anchored `/downloads/` rule. `git rm`'d it. `git status` is now clean after a test+report run.

### ECF-206 · Introduce custom exception hierarchy — ✅ DONE (bucket 1)
**Branch:** `refactor/custom-exceptions`
**Problem:** Bare `RuntimeException` everywhere ("Product not found", "Brand not found") — vague in reports, not selectively catchable.
**Delivered (scoped to UI lookup/state failures — "bucket 1"):**
- `framework/exceptions/`: abstract `FrameworkException extends RuntimeException` (two forwarding constructors — `(message)` and `(message, cause)`, no fields) + `ElementNotFoundException` + `PageStateException`. Unchecked throughout, so no signature churn.
- **`ElementNotFoundException`** — every "expected element/collection absent": product/search/carousel/cart/checkout-row lookups **and the brand/category/subcategory filter lookups** (the "Brand not found" the ticket named).
- **`PageStateException`** — postcondition/resolution failures (cart count didn't decrease [cause preserved], couldn't resolve street).
- Type carries the category; the throw site supplies the descriptive message. Value is readability + report grouping now, selective catchability latent (nothing catches them yet).
**Deliberately out of scope:** framework/config/driver setup errors (bucket 2 — readability-only, never selectively caught; existing logging suffices) and model field-validations (bucket 3 — left as idiomatic `IllegalArgumentException`/`IllegalStateException`; the title/DOB checks in `CreateAccountPage` were reclassified *off* the UI types to `IllegalArgumentException`, since they validate test data, not page state).
**Done when:** ✅ Lookup failures throw a descriptive typed exception; no bare `RuntimeException` left in pages/components.

### ECF-406 · Close remaining raw interactions + finish the locator sweep — ⬜
**Branch:** `refactor/raw-interactions-and-locators`
**Problem (2026-09-19 review):** Six raw `.click()` and two raw `driver.findElement` survive (see the P0 correction). `ProductDetailsPage.addToCartButton` is `//button[@type='button']` — the first generic button on the page. 18 exact-class XPaths (`[@class='nav nav-pills nav-stacked']`, `[@class='alert-success alert']`, …) are class-order/whitespace-sensitive where a CSS class selector is token-based. XPath and CSS are mixed for identical attribute selectors even inside one class (`NavBar`: `//a[@href='/view_cart']` vs `a[href='/delete_account']`). `BrandFilterComponent` / `CategoryFilterComponent` read their lists with an unwaited `driver.findElements`, so a slow render fails as "Brand not found" instead of a timeout. `click(By)`'s retry re-locates via *visibility* (weaker than the clickability of the first attempt) and its warn log omits the locator/exception type, unlike `retryOnStale`.
**Scope:**
- Route the six raw clicks through `click(WebElement)`; the carousel one first (Bootstrap carousels clone slides — `findFirst` can hit a hidden clone).
- Scope the product-page add-to-cart button (`button.cart` / inside `.product-information`).
- Convert `[@class='…']` XPaths to CSS class selectors; one style per selector kind (attribute selectors → CSS, text/ancestor logic → XPath).
- Wait for the filter lists before streaming them; make the `click(By)` retry re-locate via clickability and log locator + exception type.
- Fix or delete `ContactPage.uploadFile` (points at a `testfile.txt` that isn't in the repo; never called) — pairs with the contact-upload case in ECF-412.
**Done when:** `grep -rn '\.click()' src/main/java | grep -v BaseComponent` is empty; no `[@class='` XPaths remain; the generic button locator is gone.

### ECF-407 · Framework unit tests (no browser) — ⬜
**Branch:** `test/framework-unit-tests`
**Problem:** The pure logic — `TextNormalizer`, `AddressNormalizer.parseAddressName`, `ProductTextParser.parsePrice` / `parseTextAndTrim`, `ConfigReader` precedence (sysprop > env > file) and env-key mapping, `AccountRegistrationData.getAddressRegistrationData`, `CheckoutPage`'s street-resolution heuristic, the model builders' validation — has zero tests. A regex bug today surfaces only after a 20-second browser run, as a UI failure. "The framework tests itself" is a strong hiring signal and the cheapest coverage in the repo.
**Scope:**
- A `unit` TestNG group (or a second surefire execution) that runs without `BaseTest` / WebDriver in seconds; wired into the ECF-401 `lint` job so it runs before the UI suite.
- Cover the utilities and builders above; extract the `CheckoutPage` street heuristic into a pure helper so it can be tested without a `WebElement`.
- Optional: AssertJ for these tests (`isEqualByComparingTo` also retires the manual `BigDecimal.compareTo == 0` convention — see ECF-305).
**Done when:** Utilities and models have unit coverage; CI runs them in the lint job; a deliberate regex break fails in seconds, not minutes.

### ECF-408 · CI hardening + published Allure report — ⬜
**Branch:** `ci/hardening-and-pages-report`
**Problem:** The workflow has no `timeout-minutes` (a hung browser burns the 6-hour default), no `concurrency` group (superseded runs keep going), the artifact is named "Allure report" but contains raw results, nothing renders the report, and there is no scheduled run to catch live-site drift between PRs. CI history is actually good — 14 consecutive green runs since the ECF-204/308 work — and nothing surfaces it.
**Scope:**
- `timeout-minutes` on the job; `concurrency: { group: ${{ github.workflow }}-${{ github.ref }}, cancel-in-progress: true }`.
- Rename the artifact to `allure-results`; add a step that generates the report and publishes it to GitHub Pages with history (`allure generate` + a gh-pages deploy action, or an Allure report action that keeps history), so a live report URL exists.
- Nightly `schedule` cron against `main` (live-site health), separate from PR runs.
- Job summary with pass/fail counts (surefire XML → `$GITHUB_STEP_SUMMARY`).
- Firefox matrix job stays with ECF-413 (blocked on Firefox ad-blocking).
**Done when:** The README CI badge is green and links to a live Allure report with history; a superseded run is cancelled; a hung run is killed at the timeout.

### ECF-409 · README + repo presentation — 🔶 in progress
**Branch:** `chore/externalize-secrets` (README rewrite rode along with ECF-304 on 2026-09-19)
**Problem:** The README duplicated Quick Start / Prerequisites / Run Tests, carried a stale Allure badge (2.24.0 vs 2.35.3 in the pom) and a stale "seeded credentials" limitation (gone since ECF-102), had no CI badge, no architecture explanation, and no mapping to the site's 26 published test cases (25 are covered — nobody could tell). The repo has no LICENSE and no topics.
**Delivered (2026-09-19):** README rewritten — CI badge, corrected badges, deduplicated sections, architecture section (driver lifecycle, layers, page-load contract, waits/resilience, ad-blocking, retry policy, config resolution, reporting, quality gates), config-key table, groups table, coverage map to the site's 26 test cases + the 5 extra negatives, Firefox limitation stated, roadmap link to this backlog. **ECF-306** (locator & anti-flake policy doc) is absorbed here.
**Remaining:**
- `LICENSE` file (MIT is the usual choice for a portfolio repo — your call).
- GitHub topics (`selenium`, `testng`, `allure`, `java`, `page-object-model`, `test-automation`, `github-actions`) and a repo description that matches the README.
- Add the live report link once ECF-408 publishes it.
- Correct `CLAUDE.md` (untracked): it records a `DriverProvider` interface decision that isn't in the code (`TestListeners` does `instanceof BaseTest`), the old Allure version, and the stale seeded-credentials constraint.
**Done when:** A reader understands the design without opening code; badge, license, topics and report link are in place.

### ECF-410 · Product fixture strategy — ⬜
**Branch:** `refactor/product-fixtures`
**Problem:** The `Product` enum from ECF-104 relocates catalog literals rather than decoupling from them: 3 of 34 constants are used, and `userCanAddMultipleProductsToCart` still assumes the first two cards on `/products` are Blue Top and Men Tshirt (positional coupling — a reordered catalog fails it for the wrong reason). The site serves the same 34 products (name, price, brand, category) at `GET /api/productsList`.
**Scope (now, UI-only):**
- Assert invariants instead of literals where the test's intent allows: cart line total = price × quantity; cart names == the names the test actually clicked (captured from the cards it added, not from constants).
- Shrink the enum to the constants tests use; keep `MEN_TSHIRT` full detail for the product-details test.
- `parsePrice`: make it genuinely prefix-agnostic (keep digits and `.` only), so the ECF-104 description becomes true.
**Later (after ECF-403 / REST Assured):** source expected product details from `/api/productsList` at run time so catalog changes never fail UI tests for non-UI reasons.
**Done when:** No positional catalog assumptions; no unused product constants; `parsePrice`'s behaviour matches its description.

---

## P3 — Lower (polish & governance)

### ECF-301 · Static analysis + formatting gates — ✅ DONE (build) · 🔶 NOT ENFORCED (→ ECF-401)
**Branch:** `chore/static-analysis` (merged PR #17)
**Scope:** Spotless/Checkstyle + SpotBugs or PMD in the build. Fix flags, including dead code (`TestExecutionContext.getGroupName/getParams` look unused).
**Delivered (formatter + bug-finder, per the ticket's "or" — skipped Checkstyle as redundant with Palantir + IDE):**
- **Spotless** (Palantir Java format), `spotless:check` bound to `verify` — gates the build on formatting; ran `spotless:apply` once (repo-wide reformat).
- **SpotBugs**, `spotbugs:check` bound to `verify`, with a tightly-scoped `spotbugs-exclude.xml` suppressing 2 `URF_UNREAD` false positives on `BaseTest` fields (read by test subclasses SpotBugs's main-only scan can't see).
- Cleared real findings: removed dead `TestExecutionContext.groupName/params` fields + getters (4× `EI_EXPOSE`); added `default` to `CreateAccountPage.setTitle` switch (`SF_SWITCH_NO_DEFAULT`) throwing `IllegalStateException`.
**Done when:** ✅ `mvn verify` fails on style/bug violations; baseline is clean (both gates green at zero).
**⚠️ Correction (2026-09-19 review):** the gates are bound to `verify`, but CI runs `mvn clean test` (stops before `verify`) and locally `mvn -DskipTests verify` crashes in Spotless (`NoSuchMethodError`, palantir-java-format 2.50.0 on the JDK 25 that runs Maven). SpotBugs itself passes (0 bugs). Nothing enforces formatting today — see **ECF-401**.

### ECF-302 · Locator consistency pass — ✅ DONE
**Branch:** `refactor/locator-consistency`
**Scope:** Normalize text XPath, prefer `data-qa`/id hooks where present, reduce brittle `contains(text(),...)`.
**Findings (live-DOM survey of automationexercise):** no exact `text()=` matches existed, so the "whitespace-sensitive" clause was already satisfied (all text locators used `contains()`, which tolerates whitespace). The real brittleness was matching on *displayed copy*. Checked all 8 text locators against the live DOM.
**Delivered:**
- `cartEmptyMessage` → `#empty_cart b` (id-anchored; keeps the exact "Cart is empty!" text the assertion expects — the `<p>` would have returned the whole sentence and broken `assertEquals`).
- `placeOrderButton` → `//a[@href='/payment']` (href is specific; `.check_out` is a generic class reused across 3 pages — cart checkout, order-placed download, place order).
- **View Cart dedup:** `ProductDetailsPage` had a brittle `//u[contains(text(),'View Cart')]` duplicate of what `AddToCartModalComponent` already owned. Removed it; `ProductDetailsPage` now delegates to `modal.goToCart()`. Fixed the modal component's locators to genuinely scope (`element.findElement(By.cssSelector(...))` — the old leading-`//` XPath searched from document root, ignoring the modal, and could hit the nav `/view_cart` link behind the backdrop).
- **Kept as text (no better hook exists, confirmed via DOM):** the 4 `ProductDetailsPage` label fields (plain `<p>`, and `contains(.,…)` is correct — survives the `<b>` nesting `text()` would miss) and `loggedInAsUsername` (dynamic nav text).
**Done when:** ✅ Hot-path locators use stable id/href/scoped hooks where available; brittle text XPath removed or confirmed unavoidable.
**Remaining (2026-09-19 review) → ECF-406:** 18 `[@class='…']` exact-class XPaths (class-order/whitespace-sensitive), the generic `//button[@type='button']` add-to-cart locator in `ProductDetailsPage`, and mixed XPath/CSS for identical attribute selectors (`NavBar`).

### ECF-303 · Expand negative / edge coverage — ✅ DONE
**Branch:** `test/negative-coverage`
**Scope:** Add invalid-checkout, empty-search, boundary-quantity cases. Do UI-only cases now; tag API-dependent ones as blocked.
**Delivered (4 UI negatives against real guardrails — the site has no server-side input validation, so value-validation negatives aren't assertable; see blocked list):**
- **Search:** `verifyNoProductsAreReturnedWhenSearchingNonsenseTerm` — nonsense term → `getSearchResultsCount() == 0` (`ProductsPage`; search waits on the invariant "Searched Products" title, so no-results is safe).
- **Cart:** `verifyCheckoutIsNotAvailableOnEmptyCart` — empty cart hides the checkout control (`CartPage.isCheckoutAvailable()`, optional/visibility wait).
- **Checkout (access control):** `verifyGuestUserCannotGoToCheckout` — guest proceed-to-checkout → login/register modal instead of payment (`CartPage.attemptGuestCheckout()`).
- **Newsletter (browser-native constraint):** `verifyInvalidEmailIsNotAllowedForNewsletter` — malformed email fails HTML5 `type=email` validity via `checkValidity()` (`BaseComponent.isFieldValid` → `Footer.isSubscribeEmailValid`).
- **Routing:** `verify404PageIsServedOnInvalidUrl` — bad URL serves the 404 page (raw driver, no page object — asserts on page content since WebDriver can't read HTTP status).
- Auth flow already covered by existing negatives (`loginWithInvalidCredentials`, `registerUserWithExistingEmail`).

**Blocked / tracked (not assertable — site accepts invalid input, no server-side validation):**
- **boundary-quantity < 1** — cart accepts qty `0`/negative with no rejection to assert.
- **skipped register-field validation** — invalid/blank registration fields are accepted silently.
- Both become real negatives only with an assertion point the app doesn't currently provide; revisit if the site adds validation or via **ECF-403** (API-level validation is assertable).

**Done when:** ✅ Each major flow has ≥1 negative case (or a tracked blocker); blocked ones are tracked above.
**Leftover (2026-09-19):** the nonsense-search test shipped without groups (→ ECF-405); the 404 test hardcodes the absolute URL instead of `baseUrl` (→ ECF-412).

### ECF-304 · Generate registration data with DataFaker — 🔶 in progress
**Branch:** `chore/externalize-secrets` (opened under the ticket's original name before the rescope; kept to avoid churn)
**Problem:** `AccountRegistrationTestDataFactory` and `UserIdentityDataFactory` hardcode person/address literals (`John Doe`, `123 Main St`, `San Francisco`). Fixed data hides assumptions — an apostrophe in a surname, a longer address, a different zip format never get exercised.

**Rescoped (was "move credentials/secrets out of code"):** nothing in the `testdata` factories is actually sensitive — the card numbers are fake and the target is a public demo site — so moving those literals into config/secrets would relocate them without protecting anything. The genuine secrets pattern (env var / CI secret, never in the repo) is deferred to the API work (ECF-403), where a real token exists to protect.

**Scope:**
- Add DataFaker; generate name, company, address, city, zip, phone inside the existing factories. The `Builder` + factory seam means no page or test changes.
- **Constrained fields stay controlled:** `country` and the birth day/month/year are `<select>`s consumed via `selectByVisibleText` (`CreateAccountPage:77`, `:131-134`). Random values that aren't in the option list will fail — draw these from a fixed set rather than from Faker.
- **Reproducibility:** seed the `Faker` and surface the seed on failure (listener attribute / Allure parameter — lands properly with ECF-402), so a red CI run says what data it used.
- **Parallel safety:** verify `Faker` sharing under `parallel=classes` before holding a single static instance.

**Review notes (2026-09-19, uncommitted working tree):** the rename of the model *fields* to `isNewsletterSignUp` / `isSpecialOfferSignUp` while the getters stay `getNewsletterSignUp()` / `getSpecialOfferSignUp()` inverts the JavaBeans convention — the `is` prefix belongs on the boolean *getter* (`isNewsletterSignUp()`), the field stays `newsletterSignUp`. The `requireNonBlankField` extraction and the fluent builder chain in `validRegistrationUserMale` are good; apply the same chain to the other two factory methods. `datafaker 2.7.0` is in the pom but not yet used.

**Out of scope:** `CreditCardDetailsDataFactory` — two fixed cards are correct for a form that accepts anything; randomizing adds no coverage.

**Done when:** No hardcoded person/address literals remain in the two identity/registration factories; a failing run reports its seed; suite green under `-Pci`.

### ECF-305 · Soft assertions for multi-field checks — ⬜
**Branch:** `refactor/soft-assertions`
**Scope:** Use TestNG `SoftAssert` where several fields are validated together (e.g. address comparisons, the six product-detail fields) so all mismatches report at once.
**Note (2026-09-19):** this is also where the `BigDecimal.compareTo == 0` convention could be centralised in one `assertMoneyEquals` helper — or replaced wholesale by AssertJ's `SoftAssertions` + `isEqualByComparingTo` (see ECF-407). `ProductTests.userCanAddMultipleProductsToCart:73` currently violates the convention (`assertEquals` on `BigDecimal`) and is fixed in ECF-412.
**Done when:** Multi-field verifications report every failure, not just the first.

### ECF-306 · Document locator & anti-flake policy — ⬜ → merged into ECF-409
**Branch:** `docs/flakiness-policy`
**Scope:** Short doc capturing the ad-blocking approach, locator conventions, and the resilient-click contract.
**Status (2026-09-19):** absorbed by the README overhaul — its architecture section covers ad-blocking, the click/wait contract and locator conventions. Closed as duplicate; nothing left here.

### ECF-307 · HomePage slider intermittent load failure — 🔶 RECURRED (reopened)
**Branch:** `fix/homepage-slider-flake`
**⚠️ Recurred 2026-08-17:** flake reappeared in CI on the first run of the ECF-301 PR (passed on rerun). The presence-wait fix below **reduced but did not eliminate** it — presence-of-`#slider` still intermittently times out on a cold CI runner (the node genuinely isn't in the DOM yet, not a render issue). Deferred — revisit when it becomes a frequent blocker.
**Next step (revised 2026-09-19 — do not just bump the timeout):** `driver.get` blocks on the load event and `#slider` is server-rendered, so a *presence* timeout after `get()` returns most likely means a **different document was served** (challenge / error page on a cold runner), not slow rendering — and the cookie overlay cannot affect DOM presence, so handling consent first would not help. Land the failure attachments from **ECF-402** (URL + page source), then read what page the runner actually got before choosing a fix. **ECF-404** moves the wait into the constructor (same signal, earlier).
**Problem:** `HomePage.assertOnHomePage()` waited on `homePageIdentifier` (`By.id("slider")`) via `waitForVisibleElement` and intermittently failed in headless CI — passed on commit, failed on merge, same code — a timing/transient failure, not a locator problem.
**Root cause:** `visibilityOfElementLocated` requires `isDisplayed()` == true, i.e. **non-zero rendered size**. `#slider` is a carousel whose height depends on its slide images/CSS loading; in headless CI that rendering lags, so the element is present in the DOM but has zero effective size inside the wait window → visibility times out intermittently. (Confirms why the earlier locator swap didn't help — it never touched the wait condition.)
**Delivered:**
- Added `BaseComponent.waitForElementPresence` (`presenceOfElementLocated`) — DOM-existence, no rendering requirement.
- `HomePage` load-guard now waits on **presence** of `#slider`, the correct semantic for "home DOM loaded" (presence is strictly more permissive than visibility, so low-risk). `#slider` is home-only, so presence alone is a sufficient signal.
- Dropped the redundant `getCurrentUrl().contains(BASE_URL)` assertion — `baseUrl` is contained by *every* page URL on the site, so it validated nothing the slider signal didn't already cover more precisely. Removing it also cleaned up the dead `BASE_URL` field + `ConfigReader`/`org.testng.Assert` imports and the page-object-embedded TestNG assert.
- Renamed `assertOnHomePage` → `verifyHomePageLoaded` (name states intent, not the wait mechanism); both callers (`TestFlows`, `TestAssertions`) updated.
**Verification:** stable across repeated local headless (`-Pci`) runs. A flake can't be *proven* dead locally, so final confirmation is the GitHub Actions runner (where it originally surfaced) holding green across subsequent builds.

### ECF-411 · Modernise models + coordinates — ⬜
**Branch:** `refactor/records-and-root-package`
**Problem (2026-09-19 review):** Java 21 without records: `Address` hand-writes `equals` / `hashCode` / `toString`; `CartItem`, `ProductDetails`, `TestUser`, `UserIdentityData`, `DriverContext` are record-shaped. `pom.xml` still has `groupId=org.example` and the Maven placeholder `<url>`, and the packages have no root package — the first lines a reviewer reads. Also: `CreditCardDetailsData` / `UserIdentityData` have `public` builder-taking constructors (should be private like `AccountRegistrationData`); `CreateAccountPage` uses `static final By` while every other page uses instance fields (static is the correct choice — `By` is immutable); three pages expose `public final` component fields where `BasePage` uses getters.
**Scope:**
- Records for the pure value objects (builders can stay where validation lives — a record with a static `builder()` works).
- Real coordinates (`groupId` e.g. `io.github.ssgv`), drop the placeholder URL, move code under one root package.
- One locator-declaration style (`private static final By`); component access via getters or via public finals — pick one.
- Tidy: `CreateAccountPage.setTitle` / `titleResolver` (duplicated switch, unreachable default, `"null"` sentinel); `ProductDetailsPage.setProductQuantity` double clear (leftover from ECF-105); `ProductsPage.getProductResultTitle` regex + manual capitalisation → normalise both sides with `TextNormalizer` instead; `RetryTransformer`'s `"test.env"` / `"CI"` / `"GITHUB_ACTIONS"` literals → `ConfigKeys`; `RetryAnalyzer.maxRetryCount` → `MAX_RETRY_COUNT`; `BaseComponent.isFieldValid` uses `driverContext.getDriver()` where `driver` exists.
**Done when:** No hand-written `equals` / `hashCode` on value objects; no `org.example`; one style for locators and component access.

### ECF-412 · Test-layer cleanup — ⬜
**Branch:** `test/test-layer-cleanup`
**Problem (2026-09-19 review):** No `@DataProvider` anywhere (visible for a TestNG portfolio); `userCanBrowseProductsByCategory` runs two pairs inline and the two subscription tests differ only by page. Four `PlaceOrderTests` share ~12 identical lines. `MiscellaneousTests` mixes newsletter, navigation and routing. `verifyScrollUpFunctionality` scrolls and clicks but asserts nothing; `verifyTestCases` relies only on a non-unique constructor signal. `verify404PageIsServedOnInvalidUrl` hardcodes the absolute URL (ignores `baseUrl`) and uses an unwaited raw `findElement`. `ProductTests.userCanAddMultipleProductsToCart:73` compares `BigDecimal` with `assertEquals` (scale-sensitive) against this backlog's own convention. Test names mix `userCan…` / `verify…` / `register…` / `submit…`. Site TC 26 (scroll up *without* the arrow) is not covered and TC 6's file-upload step is skipped.
**Scope:**
- `@DataProvider` for category/subcategory pairs, brands, and the two subscription pages.
- Extract the shared place-order journey into a `TestFlows` method; keep each test's distinct setup only.
- Split `MiscellaneousTests` → `NewsletterTests` + `NavigationTests`; build the 404 URL from `baseUrl`.
- Real assertions for scroll-up (e.g. `window.scrollY == 0` via JS, or the header back in the viewport) and for the Test Cases page (title text); add TC 26 and the contact-form upload (needs a checked-in `testfile.txt` + the `uploadFile` fix from ECF-406).
- Fix the `BigDecimal` compare; one naming convention (`userCan…` reads best).
**Done when:** Every test asserts something explicit; no duplicated journey code; all 26 site cases mapped; DataProviders in use.

### ECF-413 · Firefox parity or de-scope — ⬜
**Branch:** `fix/firefox-ad-blocking`
**Problem (2026-09-19 review):** The README offers `-Dbrowser=firefox`, but ad-blocking is implemented with Chrome-only `--host-resolver-rules`, so Firefox runs reintroduce the P0 click-interception problem. `--host-rules` and `--host-resolver-rules` are both set with different host lists (redundant / confusing), and `--no-sandbox` is on unconditionally, including locally.
**Scope:**
- Firefox: block the same hosts via a preference-level mechanism or lightweight proxy / BiDi network interception; if none is clean, drop Firefox from the README's supported list and fail fast in `DriverFactory` with a clear message.
- Chrome: keep one host-blocking flag with one list; make `--no-sandbox` CI/headless-only.
- Then the Chrome + Firefox matrix job in CI (ECF-201/408 follow-up).
**Done when:** `-Dbrowser=firefox -Dheadless=true` runs the suite green, or Firefox is explicitly unsupported and the README says so.

### ECF-414 · Parallel-mode contract — ⬜
**Branch:** `docs/parallel-contract`
**Problem (2026-09-19 review):** `BaseTest.flows`, `testUser` and `driverContext` are instance fields on the single class instance TestNG shares across a class's methods. That is safe under `parallel=classes` (a class's methods run sequentially on one thread) but races under `parallel=methods`. The constraint is undocumented.
**Scope:** Either document `parallel=classes` as a hard requirement (README + a guard that fails fast if `parallel=methods` is detected) or thread-confine the per-test state; verify with one `-Dtestng.parallel=methods` run.
**Done when:** The supported parallel mode is stated and enforced, or the state is thread-safe.

---

## Conventions & carry-over notes

- **Money comparisons use `BigDecimal.compareTo(...) == 0`, not `.equals()`** — `equals` is scale-sensitive (`400` ≠ `400.00`), so a numerically-correct price can fail on a scale mismatch. Use `compareTo` for any `BigDecimal` price/total assertion, with a failure message carrying both values.
- **Cart-row lookup has two variants by intent** — `waitForCartRows()` (waits; for readers that expect rows) vs `getCurrentCartRows()` (`findElements`, instant snapshot; for `clearCart`/count checks). Don't point a "maybe empty" caller at the waiting one — it times out on zero.
- **Status claims must be grep-verifiable** — the 2026-09-19 review found three "done" claims the tree didn't back (raw clicks, currency-agnostic parsing, enforced gates). When closing a ticket, put the command that proves it in *Done when* (e.g. `grep -rn '\.click()' src/main/java | grep -v BaseComponent` → empty).

---

## Deferred (later stage — after the REST Assured course)

### ECF-403 · API-based account lifecycle — ⏸ deferred
**Branch:** `feat/api-account-lifecycle`
**Why deferred:** REST Assured is being learned right now; this ticket is the natural capstone for it and should not be built with an ad-hoc client first.
**Problem:** The site publishes an account API (API 11 create, API 12 delete, API 7 verify login, API 14 get by email) and the suite never uses it. Ten tests register an account through the browser, and the try/finally + `homePage = null` sentinel is copy-pasted seven times. Every destructive test pays ~20–30 s of UI setup that is not what it tests.
**Scope:**
- Thin REST Assured client (`api/` package): `createAccount`, `deleteAccount`, `verifyLogin`, `getUserByEmail`; base URL from config; if any auth is ever needed, that token becomes the first real env/CI secret.
- Opt-in provisioning: an annotation or group (`needs_account`) that makes `BaseTest` create the account via API in `@BeforeMethod` and delete it via API in `@AfterMethod`; remove the seven try/finally blocks and `AccountCleanupHelper`.
- Registration tests (TC 1, TC 5) keep the UI path — registration *is* what they test.
- Unblocks: ECF-102's orphan limitation (cleanup no longer needs a logged-in `HomePage`), ECF-303's blocked negatives (API-level input validation is assertable), ECF-410's API-sourced product expectations.
**Done when:** Only registration tests register through the UI; destructive suite time drops measurably; `AccountCleanupHelper` is gone.

---

## Suggested order

Finish **ECF-304** (in progress) → **401** (gates real; smallest credibility fix) → **405** (small, mechanical) → **404** → **402** (the report is the demo) → **406** → **407** → **408** → **409** remaining → **410** → P3 (**411** → **412** → **413** → **414**) → **403** once the REST Assured course is done.
