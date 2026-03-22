package tests.contact;

import framework.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ContactPage;
import pages.HomePage;

public class ContactUsTests extends BaseTest {
    private static final String FORM_SUCCESS_MESSAGE = "Success! Your details have been submitted successfully.";

    @Test(groups = {"smoke", "regression", "contact", "non_destructive", "fast"})
    public void submitContactUsForm() {
        HomePage homePage = flows.openHomePage();
        ContactPage contactPage = homePage.getNavBar().navigateToContactUs();
        contactPage.enterContactFormDetails("Simeon Ivanov", "simeon@test.com", "Test Subject", "This is a test message for the contact us form.");
        contactPage.submitContactForm();
        Assert.assertEquals(contactPage.getContactFormSuccessMessage(), FORM_SUCCESS_MESSAGE);
        contactPage.continueToHome();
    }
}
