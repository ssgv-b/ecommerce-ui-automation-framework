package components;

import framework.drivers.DriverContext;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseComponent {
    protected final DriverContext driverContext;
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final WebDriverWait optionalWait;

    public BaseComponent(DriverContext driverContext) {
        this.driverContext = driverContext;
        this.driver = driverContext.getDriver();
        this.wait = new WebDriverWait(driver, driverContext.getWaitDuration());
        this.optionalWait = new WebDriverWait(driver, driverContext.getOptionalWaitDuration());
    }

    protected void click(By locator) {
        WebElement element = waitForClickable(locator);
        try {
            element.click();
        } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
            log.warn("Unable to click on element, retrying.");
            WebElement updated = waitAndScrollToElement(locator);
            updated.click();
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
        retryOnStale(locator, element -> {
            element.clear();
            element.sendKeys(text);
        });
    }

    protected WebElement waitForVisibleElement(By locator) {
        log.debug("Waiting for element: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> waitForElements(By locator) {
        log.debug("Waiting for elements to be visible: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected boolean waitAndVerifyElementInvisibility(By locator) {
        log.debug("Waiting for invisibility of element: {}", locator);
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForElementPresence(By locator) {
        log.debug("Waiting for element to be present: {}", locator);
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected String getTextWhenVisible(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return element.getText();
    }

    protected WebElement waitAndScrollToElement(By locator) {
        WebElement element = waitForVisibleElement(locator);
        new Actions(driver).scrollToElement(element).perform();
        return element;
    }

    protected WebElement waitForClickable(By locator) {
        log.debug("Waiting for element to be clickable: {}", locator);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected Optional<WebElement> waitForOptionalElement(By locator) {
        log.debug("Waiting for optional element to be visible: {}", locator);
        try {
            return Optional.of(optionalWait.until(ExpectedConditions.visibilityOfElementLocated(locator)));
        } catch (TimeoutException ignored) {
            return Optional.empty();
        }
    }

    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    private void retryOnStale(By locator, Consumer<WebElement> action) {
        try {
            action.accept(waitForVisibleElement(locator));
        } catch (StaleElementReferenceException e) {
            log.warn("Retrying stale element: {}", locator);
            action.accept(waitForVisibleElement(locator));
        }
    }

    protected boolean isFieldValid(WebElement element) {
        Boolean isValid = (Boolean) ((JavascriptExecutor) driverContext.getDriver())
                .executeScript("return arguments[0].checkValidity();", element);
        return Boolean.TRUE.equals(isValid);
    }
}
