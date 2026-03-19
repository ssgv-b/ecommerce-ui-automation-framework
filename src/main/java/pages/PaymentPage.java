package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import models.CreditCardDetailsData;
import org.openqa.selenium.By;

public class PaymentPage extends BasePage {
    public PaymentPage(DriverContext driverContext) {
        super(driverContext);
        waitForVisibleElement(nameOnCardInput);
    }

    private final By nameOnCardInput = By.name("name_on_card");
    private final By cardNumberInput = By.name("card_number");
    private final By cvcInput = By.name("cvc");
    private final By expiryMonthDateInput = By.name("expiry_month");
    private final By expiryYearInput = By.name("expiry_year");
    private final By payAndConfirmOrderButton = By.id("submit");

    public OrderPlacedPage enterPaymentDetailsAndPlaceOrder(CreditCardDetailsData data) {
        enterValidPaymentDetails(data);
        click(payAndConfirmOrderButton);
        return new OrderPlacedPage(driverContext);
    }

    private void enterValidPaymentDetails(CreditCardDetailsData data) {
        enterText(nameOnCardInput, data.getCardName());
        enterText(cardNumberInput, data.getCardNumber());
        enterText(cvcInput, String.valueOf(data.getCvc()));
        enterText(expiryMonthDateInput, String.valueOf(data.getExpiryMonth()));
        enterText(expiryYearInput, String.valueOf(data.getExpiryYear()));
    }
}
