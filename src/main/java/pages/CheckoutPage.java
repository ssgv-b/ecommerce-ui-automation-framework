package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import models.Address;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class CheckoutPage extends BasePage {
    public CheckoutPage(DriverContext driverContext) {
        super(driverContext);
        By checkoutPageSignal = By.xpath("//div[@data-qa='checkout-info']");
        waitForVisibleElement(checkoutPageSignal);
    }

    private final By orderCommentTextArea = By.cssSelector(".form-control");
    private final By placeOrderButton = By.xpath("//a[contains(text(),'Place Order')]");
    private final By deliveryAddressContainer = By.id("address_delivery");
    private final By invoiceAddressContainer = By.id("address_invoice");
    private final By addressName = By.cssSelector(".address_firstname.address_lastname");
    private final By addressStreet = By.cssSelector(".address_address1.address_address2");
    private final By addressCityStateZip = By.cssSelector(".address_city.address_state_name.address_postcode");
    private final By addressCountry = By.cssSelector(".address_country_name");
    private final By addressPhone = By.cssSelector(".address_phone");

    public void enterOrderComment(String comment) {
        enterText(orderCommentTextArea, comment);
    }

    public PaymentPage placeOrder() {
        click(placeOrderButton);
        return new PaymentPage(driverContext);
    }

    public Address getCheckoutAddress(By addressType) {
        WebElement address = driver.findElement(addressType);
        String name = address.findElement(addressName).getText();
        String street = getNormalizedStreetAddress(address);
        String cityStateZip = address.findElement(addressCityStateZip).getText();
        String country = address.findElement(addressCountry).getText();
        String phone = address.findElement(addressPhone).getText();
        return new Address(name, street, cityStateZip, country, phone);
    }

    public Address getDeliveryAddress() {
        return getCheckoutAddress(deliveryAddressContainer);
    }

    public Address getInvoiceAddress() {
        return getCheckoutAddress(invoiceAddressContainer);
    }

    private String getNormalizedStreetAddress(WebElement address) {
        List<WebElement> addressValues = address.findElements(addressStreet);
        if (addressValues.isEmpty()) {
            throw new IllegalStateException("Expected address rows but received none.");
        }

        // Prefer stable index mapping when company row is present (0: company, 1: address1, 2: address2).
        String address1 = getNonBlankAt(addressValues, 1);
        String address2 = getNonBlankAt(addressValues, 2);

        // Fallback for layouts where company is omitted and only address lines are rendered.
        if (address1 == null) {
            address1 = getFirstNonBlank(addressValues);
            address2 = getSecondNonBlank(addressValues);
        }

        if (address1 == null) {
            throw new IllegalStateException("Could not resolve primary street address from checkout page.");
        }
        if (address2 == null || address2.isBlank()) {
            return address1;
        }
        return address1 + " " + address2;
    }

    private String getNonBlankAt(List<WebElement> values, int index) {
        if (index >= values.size()) {
            return null;
        }
        String value = values.get(index).getText().trim();
        return value.isBlank() ? null : value;
    }

    private String getFirstNonBlank(List<WebElement> values) {
        for (WebElement value : values) {
            String text = value.getText().trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    private String getSecondNonBlank(List<WebElement> values) {
        int seen = 0;
        for (WebElement value : values) {
            String text = value.getText().trim();
            if (text.isBlank()) {
                continue;
            }
            seen++;
            if (seen == 2) {
                return text;
            }
        }
        return null;
    }

}

