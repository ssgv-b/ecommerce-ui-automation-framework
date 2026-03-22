package tests.checkout;

import framework.base.BaseTest;
import framework.utils.TestAssertions;
import org.testng.annotations.Test;
import pages.*;
import static framework.utils.TestAssertions.assertRegistrationAndCheckoutAddressesMatch;

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
}
