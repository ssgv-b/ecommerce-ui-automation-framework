package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import org.openqa.selenium.By;

public class OrderPlacedPage extends BasePage {
    public OrderPlacedPage (DriverContext driverContext) {
        super(driverContext);
        By orderPlacedTitle = By.xpath("//h2[@data-qa='order-placed']/b");
        waitForVisibleElement(orderPlacedTitle);
    }
    private final By continueButton = By.xpath("//a[@data-qa='continue-button']");
    private final By downloadInvoiceButton = By.cssSelector(".check_out");
    private final By orderSuccessMessage = By.cssSelector("div[class='col-sm-9 col-sm-offset-1'] p");

    public HomePage continueShopping() {
        click(continueButton);
        return new HomePage(driverContext);
    }

    public void downloadInvoice() {
        click(downloadInvoiceButton);

    }

    public String getOrderSuccessMessage() {
        return getTextWhenVisible(orderSuccessMessage);
    }
}
