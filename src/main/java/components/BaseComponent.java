package components;

import framework.drivers.DriverContext;
import framework.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


public class BaseComponent {
    protected final DriverContext driverContext;
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final TextNormalizer normalizer;

    public BaseComponent(DriverContext driverContext) {
        this.driverContext = driverContext;
        this.driver = driverContext.getDriver();
        this.wait = new WebDriverWait(driver, driverContext.getWaitDuration());
        this.normalizer = new TextNormalizer();
    }

    public void click(By locator) {
        WebElement element = waitForClickable(locator);
        element.click();

    }
    public void selectByVisibleText(By locator, String value) {
        WebElement element = waitForVisibleElement(locator);
        Select select = new Select(element);
        select.selectByVisibleText(value);
    }

    public void selectByValue(By locator, String value) {
        WebElement element = waitForVisibleElement(locator);
        Select select = new Select(element);
        select.selectByValue(value);
    }

    public void enterText(By locator, String text) {
        WebElement element = waitForVisibleElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    public void enterTextNoClearing(By locator, String text) {
        WebElement element = waitForClickable(locator);
        element.sendKeys(text);
    }

    public WebElement waitForVisibleElement(By locator) {
         return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void waitForInvisibility(By locator) {
         wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public String getTextWhenVisible(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return element.getText();
    }

    public void waitAndScrollToElement(By locator) {
        WebElement element = waitForVisibleElement(locator);
        new Actions(driver)
                .scrollToElement(element)
                .perform();
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

}
