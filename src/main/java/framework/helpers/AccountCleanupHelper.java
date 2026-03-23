package framework.helpers;

import org.slf4j.LoggerFactory;
import pages.HomePage;
import org.slf4j.Logger;

public class AccountCleanupHelper {

    private AccountCleanupHelper() {

    }
    // used as a teardown safety net if test bailed before reaching TestAssertion acc deletion
    private static final Logger log = LoggerFactory.getLogger(AccountCleanupHelper.class);

    public static void deleteAccountIfPossible(HomePage homePage) {
        if (homePage == null) {
            return;
        }
        try {
            homePage.getNavBar().navigateToDeleteAccount().continueToHomePage();
        } catch (RuntimeException e) {
            log.warn("Could not delete account: {}", e.getMessage());
        }
    }
}
