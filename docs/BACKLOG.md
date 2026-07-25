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

### ECF-203 · Pin browser versions in CI — ⬜
**Branch:** `ci/pin-browser-versions`
**Problem:** Selenium Manager auto-resolution causes version drift in CI.
**Scope:** Pin Chrome/Firefox (and driver) versions in the CI environment. (Depends on ECF-201.)
**Done when:** CI logs show a fixed, intentional browser version.

### ECF-204 · Revisit wait timeouts — ⬜
**Branch:** `tune/wait-timeouts`
**Problem:** 5s local / 8s CI is tight for this target; re-measure now that ads are blocked.
**Scope:** Re-baseline `waitTimeout`/`pageLoadTimeout` against the ad-blocked site; adjust config.
**Done when:** A 3× back-to-back suite run is green with no timeout-related flakes.

### ECF-205 · Repo hygiene cleanup — ⬜
**Branch:** `chore/repo-hygiene`
**Problem:** Tracked build output (`allure-report/`, multiple `allure-results/`, `downloads/invoice.txt`) and `.DS_Store` files pollute the tree even though gitignored.
**Scope:** `git rm --cached` the tracked output/artifacts; verify `.gitignore` covers all of them.
**Done when:** `git status` is clean after a full test+report run; no generated artifacts tracked.

### ECF-206 · Introduce custom exception hierarchy — ⬜
**Branch:** `refactor/custom-exceptions`
**Problem:** Bare `RuntimeException` everywhere ("Product not found", "Brand not found") — vague in reports, not selectively catchable.
**Scope:** Small hierarchy (e.g. `ElementNotFoundException`, `TestDataException`); replace generic throws in pages/components/factories.
**Done when:** Lookup failures throw a descriptive typed exception that reads cleanly in Allure.

---

## P3 — Lower (polish & governance)

### ECF-301 · Static analysis + formatting gates — ⬜
**Branch:** `chore/static-analysis`
**Scope:** Spotless/Checkstyle + SpotBugs or PMD in the build. Fix flags, including dead code (`TestExecutionContext.getGroupName/getParams` look unused).
**Done when:** `mvn verify` fails on style/bug violations; baseline is clean.

### ECF-302 · Locator consistency pass — ⬜
**Branch:** `refactor/locator-consistency`
**Scope:** Normalize text XPath to `normalize-space()`, prefer `data-qa` hooks where present, reduce brittle `contains(text(),...)`.
**Done when:** No raw whitespace-sensitive text XPath remains in hot-path components.

### ECF-303 · Expand negative / edge coverage — ⬜
**Branch:** `test/negative-coverage`
**Scope:** Add invalid-checkout, empty-search, boundary-quantity cases. Do UI-only cases now; tag API-dependent ones as blocked.
**Done when:** Each major flow has at least one negative case; blocked ones are tracked.

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

### ECF-307 · HomePage slider intermittent load failure — ⬜
**Branch:** `fix/homepage-slider-flake`
**Problem:** `HomePage.assertOnHomePage()` waits on `homePageIdentifier` (`By.id("slider")`) and has intermittently failed to find it in headless CI — passed on commit, failed on merge, same code — i.e. a timing/transient failure, not a locator problem.
**Important:** A previous `By.id("slider")` → `By.xpath("//section[@id='slider']")` swap was tried and reverted — it targets the same element, so it does **not** fix a timing flake. `main` currently has `By.id("slider")`.
**Scope (if it recurs):** Fix the *wait/synchronization*, not the locator — e.g. handle the cookie-consent step before asserting the slider, or give the home signal a dedicated longer wait. Until it recurs, treat as a latent risk.
**Done when:** HomePage load is stable across repeated headless CI runs without a locator hack.

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
