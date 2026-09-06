package tests.checkout;

import static framework.utils.TestAssertions.assertRegistrationAndCheckoutAddressesMatch;

import framework.base.BaseTest;
import framework.utils.TestAssertions;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;

public class CheckoutTests extends BaseTest {

    @Test(groups = {"regression", "checkout", "data_integrity", "destructive", "slow"})
    public void verifyAddressDetailsDuringCheckout() {
        CreateAccountPage createAccountPage = flows.beginUserRegistration(testUser.getIdentity());
        AccountCreatedPage accountCreatedPage = createAccountPage.registerAccount(testUser.getProfile());
        HomePage homePage = accountCreatedPage.continueToHomePage();

        TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());
        ProductsPage productsPage = homePage.getNavBar().navigateToProducts();
        productsPage.addProductsToCart(1);
        CartPage cartPage = productsPage.getNavBar().navigateToCart();

        CheckoutPage checkoutPage = cartPage.goToCheckoutLoggedIn();
        assertRegistrationAndCheckoutAddressesMatch(checkoutPage, testUser.getProfile());
        AccountDeletedPage accountDeletedPage = checkoutPage.getNavBar().navigateToDeleteAccount();
        accountDeletedPage.continueToHomePage();
    }

    @Test(groups = {"regression", "checkout", "fast", "negative"})
    public void verifyCheckoutIsNotAvailableOnEmptyCart() {
        HomePage homePage = flows.openHomePage();
        CartPage cartPage = homePage.getNavBar().navigateToCart();
        cartPage.clearCart();
        Assert.assertFalse(cartPage.isCheckoutAvailable(), "Checkout should not be available with an empty cart");
    }

    @Test(groups = {"regression", "checkout", "fast", "negative"})
    public void verifyGuestUserCannotGoToCheckout() {
        ProductsPage productsPage = flows.openProductsPage();
        CartPage cartPage = flows.addProductsAndGoToCart(productsPage, 2);
        Assert.assertTrue(cartPage.attemptGuestCheckout(), "A guest user should not be able to go to checkout");
    }
}
