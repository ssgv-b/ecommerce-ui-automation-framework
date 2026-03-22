package framework.base;

import constants.ConfigKeys;
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

public class BaseTest {
    protected TestFlows flows;
    private DriverContext driverContext;
    protected TestUser testUser;

    @BeforeMethod(alwaysRun = true)
    public void setUp(){
        initDriver();
        navigateToBaseUrl();
        initFlows();
        createTestUser();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    private void initDriver() {
        driverContext = DriverFactory.initializeDriver();
    }

    private void navigateToBaseUrl() {
        getDriver().get(ConfigReader.getProperty(ConfigKeys.BASE_URL));
    }

    private void initFlows() {
        flows = new TestFlows(driverContext);
    }

    private void createTestUser() {
        UserIdentityData identity = getUniqueUser();
        AccountRegistrationData profile = getMinimalAccountRegUser();
        testUser = new TestUser(profile,identity);
    }

    private UserIdentityData getUniqueUser() {
       return UserIdentityDataFactory.newUniqueUser();
    }

    private AccountRegistrationData getMinimalAccountRegUser(){
        return AccountRegistrationTestDataFactory.minimalRegistrationUser();
    }

    public WebDriver getDriver() {
        return driverContext.getDriver();
    }

}
