package components;

import framework.drivers.DriverContext;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;


public class BaseComponent {
    protected final DriverContext driverContext;
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public BaseComponent(DriverContext driverContext) {
        this.driverContext = driverContext;
        this.driver = driverContext.getDriver();
        this.wait = new WebDriverWait(driver, driverContext.getWaitDuration());
    }

    protected void click(By locator) {
        WebElement element = waitForClickable(locator);
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Unable to click on element, retrying.");
           WebElement updatedElement =  waitAndScrollToElement(locator);
            updatedElement.click();
        }

    }
    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    protected void selectByVisibleText(By locator, String value) {
        WebElement element = waitForClickable(locator);
        Select select = new Select(element);
        select.selectByVisibleText(value);
    }

    protected void selectByValue(By locator, String value) {
        WebElement element = waitForClickable(locator);
        Select select = new Select(element);
        select.selectByValue(value);
    }

    protected void enterText(By locator, String text) {
        WebElement element = waitForVisibleElement(locator);
        element.sendKeys(text);
    }

    protected void enterTextNoClearing(By locator, String text) {
        WebElement element = waitForVisibleElement(locator);
        element.sendKeys(text);
    }

    protected WebElement waitForVisibleElement(By locator) {
        log.debug("Waiting for element: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> waitForElements (By locator) {
        log.debug("Waiting for elements to be visible: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected boolean waitAndVerifyElementInvisibility(By locator) {
        log.debug("Waiting for invisibility of element: {}", locator);
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected String getTextWhenVisible(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return element.getText();
    }

    protected WebElement waitAndScrollToElement(By locator) {
        WebElement element = waitForVisibleElement(locator);
        new Actions(driver)
                .scrollToElement(element)
                .perform();
        return element;
    }

    protected WebElement waitForClickable(By locator) {
        log.debug("Waiting for element to be clickable: {}", locator);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

}
