package tests.login;

import framework.base.BaseTest;
import framework.helpers.AccountCleanupHelper;
import framework.listeners.RetryAnalyzer;
import framework.testdata.AccountRegistrationTestDataFactory;
import framework.testdata.UserIdentityDataFactory;
import framework.utils.TestAssertions;
import models.AccountRegistrationData;
import models.TestUser;
import models.UserIdentityData;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;

public class LoginTests extends BaseTest {
    private static final String INVALID_LOGIN_ERROR = "Your email or password is incorrect!";
    private static final String EXISTING_EMAIL_ERROR = "Email Address already exist!";

    @Test(
            retryAnalyzer = RetryAnalyzer.class,
            groups = {"regression", "auth", "destructive", "slow"})
    public void registerUserAndDelete() {
        UserIdentityData userData = UserIdentityDataFactory.newUniqueUser();
        AccountRegistrationData registrationData = AccountRegistrationTestDataFactory.validRegistrationUserMale();
        testUser = new TestUser(registrationData, userData);
        HomePage homePage = null;

        try {
            LoginPage loginPage = flows.openLoginPage();
            homePage = flows.registerAndContinueToHomePage(loginPage, testUser);
            TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());
            TestAssertions.deleteAccountAndAssertHomePage(homePage);
            homePage = null;
        } finally {
            AccountCleanupHelper.deleteAccountIfPossible(homePage);
        }
    }

    @Test(groups = {"smoke", "regression", "auth", "critical_path", "destructive", "slow"})
    public void createAccountLoginAndDelete() {
        CreateAccountPage createAccountPage = flows.beginUserRegistration(testUser.getIdentity());
        LoginPage loginPage = flows.createMinimalAccountAndLogOut(createAccountPage, testUser.getProfile());
        HomePage homePage = loginPage.logInAccount(testUser.getIdentity());
        TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());
        TestAssertions.deleteAccountAndAssertHomePage(homePage);
    }

    @Test(groups = {"smoke", "regression", "auth", "negative", "non_destructive", "fast"})
    public void loginWithInvalidCredentials() {
        LoginPage loginPage = flows.openLoginPage();
        UserIdentityData userData = UserIdentityDataFactory.invalidUser();
        loginPage.logInAccount(userData);
        String errorMessage = loginPage.getLoginErrorMessage();
        Assert.assertEquals(errorMessage, INVALID_LOGIN_ERROR);
    }

    @Test(groups = {"regression", "auth", "negative", "destructive", "slow"})
    public void registerUserWithExistingEmail() {
        HomePage homePage = null;
        try {
            LoginPage loginPage = flows.registerAndLogOut(testUser);
            loginPage.createAccount(testUser.getIdentity());
            String errorMessage = loginPage.getExistingEmailErrorMessage();
            Assert.assertEquals(errorMessage, EXISTING_EMAIL_ERROR);

            homePage = loginPage.logInAccount(testUser.getIdentity());
            TestAssertions.deleteAccountAndAssertHomePage(homePage);
            homePage = null;
        } finally {
            AccountCleanupHelper.deleteAccountIfPossible(homePage);
        }
    }

    @Test(groups = {"regression", "auth", "destructive", "slow"})
    public void createAccountAndLogOut() {
        LoginPage loginPage = flows.openLoginPage();
        HomePage homePage = flows.registerAndContinueToHomePage(loginPage, testUser);
        TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());
        LoginPage loggedOutPage = homePage.getNavBar().logOut();

        // Clean up the account created above so it is not orphaned on the shared site.
        HomePage reloggedHomePage = loggedOutPage.logInAccount(testUser.getIdentity());
        TestAssertions.deleteAccountAndAssertHomePage(reloggedHomePage);
    }
}
