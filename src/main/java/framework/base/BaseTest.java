package framework.base;

import flows.TestFlows;
import framework.drivers.DriverContext;
import framework.drivers.DriverFactory;
import framework.testdata.AccountRegistrationTestDataFactory;
import framework.testdata.UserIdentityDataFactory;
import framework.utils.ConfigReader;
import models.AccountRegistrationData;
import models.TestUser;
import models.UserIdentityData;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.CreateAccountPage;
import pages.HomePage;
import pages.LoginPage;
import pages.ProductsPage;

public class BaseTest {
    protected WebDriver driver;
    protected TestFlows flows;
    protected DriverContext driverContext;
    protected TestUser testUser;

    @BeforeMethod(alwaysRun = true)
    public void setUp(){
        initDriver();
        initFlows();
        initTestUser();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null)
            driver.quit();
    }

    private void initDriver() {
        DriverFactory driverFactory = new DriverFactory();
        driverContext = driverFactory.initializeDriver();
        driver = driverContext.getDriver();
        String baseURL = ConfigReader.getProperty("baseUrl");
        driver.get(baseURL);
    }

    private void initFlows() {
        flows = new TestFlows(driverContext);
    }

    private void createTestUser() {
        UserIdentityData identity = getUniqueUser();
        AccountRegistrationData profile = getMinimalAccountRegUser();
        testUser = new TestUser(profile,identity);
    }

    private void initTestUser() {
        createTestUser();
    }

    protected HomePage openHomePage() {
        HomePage homePage = new HomePage(driverContext);
        homePage.assertOnHomePage();
        return homePage;
    }

    protected ProductsPage openProductsPage() {
        HomePage homePage = openHomePage();
        return homePage.getNavBar().navigateToProducts();
    }

    protected HomePage loginAsExistingUser(UserIdentityData identity) {
        LoginPage loginPage = openLoginPage();
        return loginPage.logInAccount(identity);
    }

    protected CreateAccountPage beginUserRegistration(UserIdentityData identity) {
        LoginPage loginPage = openLoginPage();
        return loginPage.createAccount(identity);
    }

    protected LoginPage openLoginPage() {
        HomePage homePage = openHomePage();
        return homePage.getNavBar().navigateToLogin();
    }

    protected void deleteAccountIfPossible(HomePage homePage) {
        if (homePage == null) {
            return;
        }
        try {
            homePage.getNavBar().navigateToDeleteAccount().continueToHomePage();
        } catch (RuntimeException ignored) {
            // Best-effort cleanup to avoid leaking accounts when a test fails mid-flow.
        }
    }

    private UserIdentityData getUniqueUser() {
       return UserIdentityDataFactory.newUniqueUser();
    }

    private AccountRegistrationData getMinimalAccountRegUser(){
        return AccountRegistrationTestDataFactory.minimalRegistrationUser();
    }

    public WebDriver getDriverListener() {
        return this.driver;
    }

}
