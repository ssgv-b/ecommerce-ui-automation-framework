package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import org.openqa.selenium.By;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ContactPage extends BasePage {
    public ContactPage(DriverContext driverContext) {
        super(driverContext);
        By contactPageSignal = By.xpath("//form[@id='contact-us-form']");
        waitForVisibleElement(contactPageSignal);
    }

    private final By nameInput = By.xpath("//input[@name='name']");
    private final By emailInput = By.xpath("//input[@name='email']");
    private final By subjectInput = By.xpath("//input[@data-qa='subject']");
    private final By messageInput = By.id("message");
    private final By uploadFileButton = By.xpath("//input[@type='file']");
    private final By submitFormButton = By.xpath("//input[@type='submit']");
    private final By successMessage = By.xpath("//div[@class='status alert alert-success']");
    private final By homeButton = By.xpath("//a[@class='btn btn-success']");

    public void enterContactFormDetails(String name, String email, String subject, String message) {
        enterText(nameInput, name);
        enterText(emailInput, email);
        enterText(subjectInput, subject);
        enterText(messageInput, message);
    }

    public void submitContactForm() {
        click(submitFormButton);
        acceptAlertIfPresent();

    }
    public void uploadFile() {
        Path filePath = Paths.get(System.getProperty("user.dir"), "src/test/resources/testfile.txt");
        if (!Files.exists(filePath)) {
            throw new IllegalStateException("Upload file not found: " + filePath);
        }
        driver.findElement(uploadFileButton).sendKeys(filePath.toString());
    }

    public String getContactFormSuccessMessage() {
        return getTextWhenVisible(successMessage);
    }

    public HomePage continueToHome() {
        click(homeButton);
        return new HomePage(driverContext);
    }

}