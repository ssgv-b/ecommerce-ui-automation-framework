package framework.base;

import components.BaseComponent;
import components.Footer;
import components.NavBar;
import framework.drivers.DriverContext;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage extends BaseComponent {

    private final NavBar navBar;
    private final Footer footer;
    private final By scrollTopArrow = By.id("scrollUp");

    public BasePage(DriverContext driverContext) {
        super(driverContext);
        this.navBar = new NavBar(driverContext);
        this.footer = new Footer(driverContext);
    }

    public void acceptAlertIfPresent() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.alertIsPresent())
                    .accept();
        } catch (TimeoutException ignored) {
            // no-op: some flows do not trigger a browser alert.
        }
    }

    public void scrollToTopArrow() {
       WebElement scrollElement =  waitForClickable(scrollTopArrow);
       scrollElement.click();
    }

    public NavBar getNavBar() {
        return navBar;
    }
    public Footer getFooter() {return footer; }
}
