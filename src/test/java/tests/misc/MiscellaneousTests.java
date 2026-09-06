package tests.misc;

import framework.base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.HomePage;

public class MiscellaneousTests extends BaseTest {
    private static final String SUBSCRIPTION_EMAIL = "test@testor.com";
    private static final String INVALID_SUBSCRIPTION_EMAIL = "zzxxyy";
    private static final String SUBSCRIPTION_SUCCESS_MESSAGE = "You have been successfully subscribed!";
    private static final String BAD_URL = "https://automationexercise.com/view_cart/page=1";
    private static final String PAGE_NOT_FOUND_TITLE = "Page not found (404)";
    private static final By INVALID_PAGE_HEADING = By.xpath("//div[@id='summary']/h1");

    @Test(groups = {"regression", "navigation", "non_destructive", "fast"})
    public void verifyTestCases() {
        HomePage homePage = flows.openHomePage();
        homePage.getNavBar().navigateToTestCases();
    }

    @Test(groups = {"regression", "navigation", "non_destructive", "fast"})
    public void verifySubscriptionInHomePage() {
        HomePage homePage = flows.openHomePage();
        homePage.getFooter().enterFooterEmailAndSubscribe(SUBSCRIPTION_EMAIL);
        Assert.assertEquals(homePage.getFooter().getSubscribeSuccessMessageText(), SUBSCRIPTION_SUCCESS_MESSAGE);
    }

    @Test(groups = {"regression", "navigation", "cart", "non_destructive", "fast"})
    public void verifySubscriptionInCartPage() {
        HomePage homePage = flows.openHomePage();
        CartPage cartPage = homePage.getNavBar().navigateToCart();
        cartPage.getFooter().enterFooterEmailAndSubscribe(SUBSCRIPTION_EMAIL);
        Assert.assertEquals(cartPage.getFooter().getSubscribeSuccessMessageText(), SUBSCRIPTION_SUCCESS_MESSAGE);
    }

    @Test(groups = {"regression", "navigation", "non_destructive", "fast"})
    public void verifyScrollUpFunctionality() {
        HomePage homePage = flows.openHomePage();
        homePage.getFooter().scrollToFooter();
        homePage.scrollToTopArrow();
    }

    @Test(groups = {"regression", "navigation", "non-destructive", "fast", "negative"})
    public void verify404PageIsServedOnInvalidUrl() {
        WebDriver driver = getDriver();
        driver.get(BAD_URL);
        String invalidPageHeading =
                driver.findElement(INVALID_PAGE_HEADING).getText().strip();
        Assert.assertEquals(invalidPageHeading, PAGE_NOT_FOUND_TITLE);
    }

    @Test(groups = {"regression", "navigation", "non-destructive", "fast", "negative"})
    public void verifyInvalidEmailIsNotAllowedForNewsletter() {
        HomePage homePage = flows.openHomePage();
        homePage.getFooter().scrollToFooter();
        Assert.assertFalse(
                homePage.getFooter().isSubscribeEmailValid(INVALID_SUBSCRIPTION_EMAIL),
                "A malformed email should fail the browser's email-type validation");
    }
}
