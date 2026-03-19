package tests.login;

import framework.base.BaseTest;
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

    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "auth", "destructive", "slow"})
    public void registerUserAndDelete() {
        UserIdentityData userData = UserIdentityDataFactory.newUniqueUser();
        AccountRegistrationData registrationData = AccountRegistrationTestDataFactory.validRegistrationUserFemale();
        testUser = new TestUser(registrationData, userData);
        HomePage homePage = null;

        try {
            LoginPage loginPage = openLoginPage();
            homePage = flows.registerAndContinueToHomePage(loginPage, testUser);
            TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());
            TestAssertions.deleteAccountAndAssertHomePage(homePage);
            homePage = null;
        } finally {
            deleteAccountIfPossible(homePage);
        }
    }

    @Test(groups = {"smoke", "regression", "auth", "critical_path", "destructive", "slow"})
    public void createAccountLoginAndDelete() {
        CreateAccountPage createAccountPage = beginUserRegistration(testUser.getIdentity());
        LoginPage loginPage = flows.createMinimalAccountAndLogOut(createAccountPage, testUser.getProfile());
        HomePage homePage = loginPage.logInAccount(testUser.getIdentity());
        TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());
        TestAssertions.deleteAccountAndAssertHomePage(homePage);
    }

    @Test(groups = {"smoke", "regression", "auth", "negative", "non_destructive", "fast"})
    public void loginWithInvalidCredentials() {
        LoginPage loginPage = openLoginPage();
        UserIdentityData userData = UserIdentityDataFactory.invalidUser();
        loginPage.logInAccount(userData);
        String errorMessage = loginPage.getLoginErrorMessage();
        Assert.assertEquals(errorMessage, INVALID_LOGIN_ERROR);
    }

    @Test(groups = {"regression", "auth", "non_destructive", "fast"})
    public void logInAccountAndLogOut() {
        UserIdentityData userDataExistingUser = UserIdentityDataFactory.existingSeededUser();
        HomePage homePage = loginAsExistingUser(userDataExistingUser);
        TestAssertions.assertLoggedInUser(homePage, userDataExistingUser);
        homePage.getNavBar().logOut();
    }

    @Test(groups = {"regression", "auth", "negative", "non_destructive", "fast"})
    public void registerUserWithExistingEmail() {
        LoginPage loginPage = openLoginPage();
        UserIdentityData existingEmailUserData = UserIdentityDataFactory.invalidUser();
        loginPage.createAccount(existingEmailUserData);
        String errorMessage = loginPage.getExistingEmailErrorMessage();
        Assert.assertEquals(errorMessage, EXISTING_EMAIL_ERROR);
    }

    @Test(groups = {"regression", "auth", "destructive", "slow"})
    public void createAccountAndLogOut() {
        LoginPage loginPage = openLoginPage();
        HomePage homePage = flows.registerAndContinueToHomePage(loginPage,testUser);
        TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());
        homePage.getNavBar().logOut();
    }
}
