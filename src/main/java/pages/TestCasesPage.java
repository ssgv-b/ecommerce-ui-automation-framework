package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import org.openqa.selenium.By;

public class TestCasesPage extends BasePage {
    public TestCasesPage(DriverContext driverContext) {
        super(driverContext);
        By testCasesTitle = By.xpath("//h2[@class='title text-center']");
        waitForVisibleElement(testCasesTitle);
    }
}
