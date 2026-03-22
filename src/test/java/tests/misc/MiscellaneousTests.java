package tests.misc;

import framework.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.HomePage;

public class MiscellaneousTests extends BaseTest {
    private static final String SUBSCRIPTION_EMAIL = "test@testor.com";
    private static final String SUBSCRIPTION_SUCCESS_MESSAGE = "You have been successfully subscribed!";

    @Test(groups = {"regression", "navigation", "non_destructive", "fast"})
    public void verifyTestCases() {
        HomePage homePage = flows.openHomePage();
        homePage.getNavBar().navigateToTestCases();
    }

    @Test(groups = {"regression", "navigation", "non_destructive", "fast"})
    public void verifySubscriptionInHomePage() {
        HomePage homePage = flows.openHomePage();
        homePage.waitAndScrollToElement(homePage.getFooter().getFooterSection());
        homePage.getFooter().enterFooterEmailAndSubscribe(SUBSCRIPTION_EMAIL);
        Assert.assertEquals(homePage.getFooter().getSubscribeSuccessMessageText(), SUBSCRIPTION_SUCCESS_MESSAGE);
    }

    @Test(groups = {"regression", "navigation", "cart", "non_destructive", "fast"})
    public void verifySubscriptionInCartPage() {
        HomePage homePage = flows.openHomePage();
        CartPage cartPage = homePage.getNavBar().navigateToCart();
        cartPage.waitAndScrollToElement(cartPage.getFooter().getFooterSection());
        cartPage.getFooter().enterFooterEmailAndSubscribe(SUBSCRIPTION_EMAIL);
        Assert.assertEquals(cartPage.getFooter().getSubscribeSuccessMessageText(), SUBSCRIPTION_SUCCESS_MESSAGE);
    }

    @Test(groups = {"regression", "navigation", "non_destructive", "fast"})
    public void verifyScrollUpFunctionality() {
        HomePage homePage = flows.openHomePage();
        homePage.getFooter().scrollToFooter();
        homePage.scrollToTopArrow();
    }

    @Test(groups = {"regression", "navigation", "non_destructive", "fast"})
    public void scrollToFooterAndBackToHome() {
        HomePage homePage = flows.openHomePage();
        homePage.getFooter().scrollToFooter();
        homePage.scrollToHeroSection();
    }
}
