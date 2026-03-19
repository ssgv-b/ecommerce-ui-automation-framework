package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import models.UserIdentityData;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
    public LoginPage(DriverContext driverContext) {
        super(driverContext);
        By loginPageSignal = By.xpath("//div[@class='login-form']");
        waitForVisibleElement(loginPageSignal);
    }
    private final By loginEmailInput = By.xpath("//input[@data-qa='login-email']");
    private final By loginPasswordInput = By.xpath("//input[@data-qa='login-password']");
    private final By loginButton = By.xpath("//button[@data-qa='login-button']");
    private final By signUpNameInput = By.xpath("//input[@data-qa='signup-name']");
    private final By signUpEmailInput = By.xpath("//input[@data-qa='signup-email']");
    private final By signUpButton = By.xpath("//button[@data-qa='signup-button']");
    private final By loginErrorMessage = By.xpath("//p[contains (text(),'Your email or password is incorrect')]");
    private final By createExistingEmailError = By.xpath("//p[contains (text(),'Email Address already exist!')]");

    public HomePage logInAccount(UserIdentityData userData) {
        enterText(loginEmailInput, userData.getUserEmail());
        enterText(loginPasswordInput, userData.getUserPassword());
        click(loginButton);
        return new HomePage(driverContext);
    }

    public CreateAccountPage createAccount(UserIdentityData userData) {
        enterText(signUpNameInput, userData.getUserName());
        enterText(signUpEmailInput, userData.getUserEmail());
        click(signUpButton);
        return new CreateAccountPage(driverContext, userData);

    }

    public String getLoginErrorMessage() {
        return getTextWhenVisible(loginErrorMessage);
    }

    public String getExistingEmailErrorMessage() {
        return getTextWhenVisible(createExistingEmailError);
    }

}
