package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import org.openqa.selenium.By;

public class AccountDeletedPage extends BasePage {
    public AccountDeletedPage (DriverContext driverContext) {
        super(driverContext);
        By accountDeletedPageSignal = By.xpath("//h2[@data-qa='account-deleted']/b");
        waitForVisibleElement(accountDeletedPageSignal);
    }
    private final By continueButton = By.xpath("//a[@data-qa='continue-button']");

    public HomePage continueToHomePage() {
        click(continueButton);
        return new HomePage(driverContext);
    }
}
