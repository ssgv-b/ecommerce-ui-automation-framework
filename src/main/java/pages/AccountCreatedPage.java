package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import org.openqa.selenium.By;

public class AccountCreatedPage extends BasePage {
    public AccountCreatedPage (DriverContext driverContext) {
        super(driverContext);
        By accountCreatedPageSignal = By.xpath("//h2[@data-qa='account-created']/b");
        waitForVisibleElement(accountCreatedPageSignal);
    }

    private final By continueButton = By.xpath("//a[@data-qa='continue-button']");

    public HomePage continueToHomePage() {
        click(continueButton);
        return new HomePage(driverContext);
    }

}
