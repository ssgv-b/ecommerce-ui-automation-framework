# Framework Backlog

Prioritized implementation backlog from the Lead-QA framework review.
One ticket per branch. API-based test-data seeding is deferred and noted where it blocks a ticket.

Legend: ✅ done · 🔶 in progress · ⬜ not started

---

## P0 — Critical (stability) — ✅ DONE

- ✅ **Ad / overlay blocking** — implemented via Chrome host-resolver rules blocking known AdSense URLs. Ad iframes no longer load, so click interception from ads is eliminated at the source.
- ✅ **Resilient click rework** — `click(By)` now re-locates the element via `waitAndScrollToElement(locator)` on `ElementClickInterceptedException` (no more stale-reference reuse). Added a scoped `click(WebElement)` overload that waits for clickability.
- ✅ **Raw click / findElement holes closed** — interactions in `ProductsPage`, `CategoryFilterComponent`, `BrandFilterComponent`, `AddToCartModalComponent`, and `CheckoutPage` now route through the safe wrappers / scoped overload instead of raw `.click()` / `.findElement()`.

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

**Known limitation (accepted):** If a test fails *after* provisioning but *before* it logs in, the fresh account can orphan (the finally-cleanup needs a logged-in `HomePage`). Unique emails prevent collisions, so this is tolerable; a future identity-based cleanup (log in by identity, then delete) would close it — revisit alongside the API work.

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
- Added `ProductTextParser.parsePrice` (`String → BigDecimal`, currency-prefix agnostic); reused in `CartPage` and `ProductDetailsPage`, removing duplicated price stripping.
- `ProductDetails` and `CartItem` model prices as `BigDecimal`.
- Fixed `CartPage.readCartItems` to scope every field read to its row (rows 2+ were picking up row 1's price/quantity/total).
- Split cart-row lookup into a waiting variant (`getCurrentCartItems`, for readers that expect rows) and a non-waiting snapshot (`getCartItems` → `findElements`, for `clearCart`) so clearing no longer burns the wait timeout on the empty check.
- Prices compared with `compareTo(...) == 0` (scale-insensitive) with a value-carrying failure message.
- Removed dead `addToCartButtonOverlay` locator; `.gitignore` ignores `CLAUDE.md`.

**Deferred to their own branches:**
- Split categories/brands into a separate enum (still magic strings in `ProductTests`).
- Remaining `ProductTests` reworks.
- `Optional` for the nullable detail getters — nulls are load-bearing but contained to non-`MEN_TSHIRT` constants; promote the four detail fields into a grouped optional bundle if/when a second product needs full detail.

### ECF-105 · Fix `enterText` clear semantics + remove duplicate — ✅ DONE
**Branch:** `fix/entertext-clear-semantics`
**Problem:** `enterText()` and `enterTextNoClearing()` had identical bodies (neither cleared). Name implies clear-then-type; edit / re-search flows would append.
**Delivered:**
- `enterText()` now calls `element.clear()` before `sendKeys(text)` — the name finally matches the behavior.
- Audited all 28 `enterText` call sites (payment, registration, contact, login/signup, review, quantity, order comment, search): every one is empty-field-entry semantics, so clearing is safe everywhere and actively correct for `ProductsPage` search (repeated searches no longer concatenate).
- `enterTextNoClearing()` had exactly one caller (`Footer.enterFooterEmailAndSubscribe`, also an empty field) — repointed it to `enterText` and deleted the redundant method. Zero lingering references; compiles clean.
**Done when:** ✅ `enterText` clears; the redundant twin is gone.

---

## P2 — Medium

### ECF-201 · Stand up CI pipeline — ✅ DONE (core)
**Branch:** `ci/github-actions`
**Delivered:** `.github/workflows/ci.yaml` (on `origin/main`) runs `mvn clean test -Pci` headless on push/PR to main, JDK 21 (temurin) + Maven cache, and uploads `target/allure-results` as an artifact with `if: "!cancelled()"`. PRs get a green/red check.
**Remaining polish (optional, could be their own tickets):** no Chrome + Firefox matrix yet (single default-browser job — pairs with ECF-203); uploads raw Allure *results* rather than a rendered report; failure screenshots are captured inside allure-results (via the listener) but not surfaced as a standalone artifact.

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
- **Downloads don't work on Grid** — the browser downloads to the *node's* disk, not the runner's, so the file-download test times out under `-Dremote=true`. Expected; downloads were explicitly scoped out of remote. Follow-up: tag download-asserting tests so they're skipped when `remote=true` (removes the one red from remote runs).
- **Apple Silicon (ARM64):** `selenium/standalone-chrome` has no arm64 image; use `selenium/standalone-chromium` locally. Chromium is Chrome-compatible for these options, so no code change needed.

**Follow-ups (own tickets):** Chrome + Firefox matrix via hub + node (docker-compose) pairs with ECF-201/203; CI doesn't provision a Grid yet (`config-ci.properties` stays local).

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

---

## P3 — Lower (polish & governance)

### ECF-301 · Static analysis + formatting gates — ✅ DONE
**Branch:** `chore/static-analysis` (merged PR #17)
**Scope:** Spotless/Checkstyle + SpotBugs or PMD in the build. Fix flags, including dead code (`TestExecutionContext.getGroupName/getParams` look unused).
**Delivered (formatter + bug-finder, per the ticket's "or" — skipped Checkstyle as redundant with Palantir + IDE):**
- **Spotless** (Palantir Java format), `spotless:check` bound to `verify` — gates the build on formatting; ran `spotless:apply` once (repo-wide reformat).
- **SpotBugs**, `spotbugs:check` bound to `verify`, with a tightly-scoped `spotbugs-exclude.xml` suppressing 2 `URF_UNREAD` false positives on `BaseTest` fields (read by test subclasses SpotBugs's main-only scan can't see).
- Cleared real findings: removed dead `TestExecutionContext.groupName/params` fields + getters (4× `EI_EXPOSE`); added `default` to `CreateAccountPage.setTitle` switch (`SF_SWITCH_NO_DEFAULT`) throwing `IllegalStateException`.
**Done when:** ✅ `mvn verify` fails on style/bug violations; baseline is clean (both gates green at zero).

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
- Both become real negatives only with an assertion point the app doesn't currently provide; revisit if the site adds validation or via the API-data-lifecycle work below.

**Done when:** ✅ Each major flow has ≥1 negative case (or a tracked blocker); blocked ones are tracked above.

### ECF-304 · Move test credentials/data out of code — ⬜
**Branch:** `chore/externalize-secrets`
**Scope:** Move seeded-user and card data from factories into config/secrets to set the right pattern.
**Done when:** No credentials are hardcoded in `testdata` factories.

### ECF-305 · Soft assertions for multi-field checks — ⬜
**Branch:** `refactor/soft-assertions`
**Scope:** Use TestNG `SoftAssert` where several fields are validated together (e.g. address comparisons) so all mismatches report at once.
**Done when:** Multi-field verifications report every failure, not just the first.

### ECF-306 · Document locator & anti-flake policy — ⬜
**Branch:** `docs/flakiness-policy`
**Scope:** Short doc capturing the ad-blocking approach, locator conventions, and the resilient-click contract.
**Done when:** A `docs/` page describes the ad-blocking + click/wait strategy.

### ECF-307 · HomePage slider intermittent load failure — 🔶 RECURRED (reopened)
**Branch:** `fix/homepage-slider-flake`
**⚠️ Recurred 2026-08-17:** flake reappeared in CI on the first run of the ECF-301 PR (passed on rerun). The presence-wait fix below **reduced but did not eliminate** it — presence-of-`#slider` still intermittently times out on a cold CI runner (the node genuinely isn't in the DOM yet, not a render issue). **Next step (do not just bump the timeout):** handle the cookie-consent step *before* asserting the slider, or give the home signal its own dedicated longer wait. Deferred — revisit when it becomes a frequent blocker.
**Problem:** `HomePage.assertOnHomePage()` waited on `homePageIdentifier` (`By.id("slider")`) via `waitForVisibleElement` and intermittently failed in headless CI — passed on commit, failed on merge, same code — a timing/transient failure, not a locator problem.
**Root cause:** `visibilityOfElementLocated` requires `isDisplayed()` == true, i.e. **non-zero rendered size**. `#slider` is a carousel whose height depends on its slide images/CSS loading; in headless CI that rendering lags, so the element is present in the DOM but has zero effective size inside the wait window → visibility times out intermittently. (Confirms why the earlier locator swap didn't help — it never touched the wait condition.)
**Delivered:**
- Added `BaseComponent.waitForElementPresence` (`presenceOfElementLocated`) — DOM-existence, no rendering requirement.
- `HomePage` load-guard now waits on **presence** of `#slider`, the correct semantic for "home DOM loaded" (presence is strictly more permissive than visibility, so low-risk). `#slider` is home-only, so presence alone is a sufficient signal.
- Dropped the redundant `getCurrentUrl().contains(BASE_URL)` assertion — `baseUrl` is contained by *every* page URL on the site, so it validated nothing the slider signal didn't already cover more precisely. Removing it also cleaned up the dead `BASE_URL` field + `ConfigReader`/`org.testng.Assert` imports and the page-object-embedded TestNG assert.
- Renamed `assertOnHomePage` → `verifyHomePageLoaded` (name states intent, not the wait mechanism); both callers (`TestFlows`, `TestAssertions`) updated.
**Verification:** stable across repeated local headless (`-Pci`) runs. A flake can't be *proven* dead locally, so final confirmation is the GitHub Actions runner (where it originally surfaced) holding green across subsequent builds.

---

## Conventions & carry-over notes

- **Money comparisons use `BigDecimal.compareTo(...) == 0`, not `.equals()`** — `equals` is scale-sensitive (`400` ≠ `400.00`), so a numerically-correct price can fail on a scale mismatch. Use `compareTo` for any `BigDecimal` price/total assertion, with a failure message carrying both values.
- **Cart-row lookup has two variants by intent** — `waitForCartRows()` (waits; for readers that expect rows) vs `getCurrentCartRows()` (`findElements`, instant snapshot; for `clearCart`/count checks). Don't point a "maybe empty" caller at the waiting one — it times out on zero.

---

## Deferred (out of current scope)

- **API-based test-data lifecycle** — register/login/seed/cleanup via the AutomationExercise REST API instead of the UI. Biggest post-P0 leverage for speed and stability; unblocks parts of ECF-102 and ECF-303. Revisit when ready.

---

## Suggested order

ECF-101 (finish + fixes) → 103 → 105 → 102 → 104 → CI/Grid block (201 → 203, 202) → polish (P3).
