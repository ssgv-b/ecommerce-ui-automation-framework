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

### ECF-103 · Fix unique-suffix collision risk — ⬜
**Branch:** `fix/unique-user-suffix`
**Problem:** `generateUniqueSuffix()` truncates a UUID to 5 hex chars (~1M space); birthday collisions under `parallel=classes` + retries surface as spurious "Email already exists".
**Scope:** Use the full UUID (or `timestamp + threadId`) for email/username generation.
**Done when:** A stress run (~100 rapid registrations across 3 threads) produces zero duplicate-email failures.

### ECF-104 · Decouple tests from brittle catalog data — ⬜
**Branch:** `refactor/externalize-product-expectations`
**Problem:** `ProductTests` asserts exact literals (`Rs. 400`, `H&M`, `Men > Tshirts`); catalog changes fail tests for unrelated reasons.
**Scope:** Move expected product fixtures to a constants/data source; pin assertions to the most stable products; document which products are treated as stable fixtures.
**Done when:** Expected values live in one place, not inline across test methods.

### ECF-105 · Fix `enterText` clear semantics + remove duplicate — ⬜
**Branch:** `fix/entertext-clear-semantics`
**Problem:** `enterText()` and `enterTextNoClearing()` have identical bodies (neither clears). Name implies clear-then-type; edit flows will append.
**Scope:** Make `enterText()` clear before typing. Keep `enterTextNoClearing()` only if a caller needs append; otherwise delete it. Audit call sites.
**Done when:** `enterText` clears; the two methods are meaningfully different or the redundant one is gone.

---

## P2 — Medium

### ECF-201 · Stand up CI pipeline — ⬜
**Branch:** `ci/github-actions`
**Problem:** A `ci` Maven profile and `config-ci.properties` exist but nothing runs them.
**Scope:** `.github/workflows/` running `mvn clean test -Pci` headless on push/PR; Chrome + Firefox matrix; publish Allure results and upload failure screenshots as artifacts.
**Done when:** PRs get a green/red check plus downloadable Allure report + screenshots.

### ECF-202 · Add RemoteWebDriver / Grid support — ⬜
**Branch:** `feat/remote-driver`
**Problem:** `DriverFactory` only builds local drivers; can't scale or stabilize CI browser versions.
**Scope:** Config flag (`remote=true` + hub URL) selecting `RemoteWebDriver`; local stays default; reuse existing options builders.
**Done when:** Same suite runs unchanged against a local Selenium Grid via config only.

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

---

## Deferred (out of current scope)

- **API-based test-data lifecycle** — register/login/seed/cleanup via the AutomationExercise REST API instead of the UI. Biggest post-P0 leverage for speed and stability; unblocks parts of ECF-102 and ECF-303. Revisit when ready.

---

## Suggested order

ECF-101 (finish + fixes) → 103 → 105 → 102 → 104 → CI/Grid block (201 → 203, 202) → polish (P3).
