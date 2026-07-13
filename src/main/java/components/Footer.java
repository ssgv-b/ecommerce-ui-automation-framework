package components;

import framework.drivers.DriverContext;
import org.openqa.selenium.By;

public class Footer extends BaseComponent{

    public Footer (DriverContext driverContext){
        super(driverContext);
    }

    private final By footerSection = By.id("footer");
    private final By subscriptionInput = By.id("susbscribe_email");
    private final By subscribeButton = By.id("subscribe");
    private final By subscribeSuccessMessage = By.xpath("//div[@class='alert-success alert']");


    public void enterFooterEmailAndSubscribe(String email) {
        scrollToFooter();
        enterText(subscriptionInput, email);
        click(subscribeButton);
    }

    public String getSubscribeSuccessMessageText() {
        return getTextWhenVisible(subscribeSuccessMessage);
    }

    public void scrollToFooter() {
        waitAndScrollToElement(footerSection);
    }
}
