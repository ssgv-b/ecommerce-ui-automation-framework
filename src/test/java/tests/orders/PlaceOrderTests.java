package tests.orders;

import framework.base.BaseTest;
import framework.testdata.AccountRegistrationTestDataFactory;
import framework.testdata.CreditCardDetailsDataFactory;
import framework.testdata.UserIdentityDataFactory;
import framework.utils.ConfigReader;
import framework.utils.FileDownloadUtils;
import framework.utils.TestAssertions;
import models.AccountRegistrationData;
import models.CreditCardDetailsData;
import models.TestUser;
import models.UserIdentityData;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import static framework.utils.TestAssertions.assertDeliveryAndInvoiceAddressesMatch;

public class PlaceOrderTests extends BaseTest {
    private static final String ORDER_COMMENT = "This is a test order comment.";
    private static final String ORDER_SUCCESS_MESSAGE = "Congratulations! Your order has been confirmed!";
    private static final String INVOICE_ORDER_COMMENT = "Invoice Download Order Comment";

    @Test(groups = {"regression", "order", "checkout", "critical_path", "data_integrity", "destructive", "slow"})
    public void userCanPlaceOrderImmediatelyAfterRegistration() {
        HomePage homePage = null;
        ProductsPage productsPage = openProductsPage();
        CartPage cartPage = flows.addProductsAndGoToCart(productsPage, 2);

        try {
            LoginPage loginPage = cartPage.goToLoginFromCheckout();
            homePage = flows.registerAndContinueToHomePage(loginPage, testUser);
            TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());

            cartPage = homePage.getNavBar().navigateToCart();
            CheckoutPage checkoutPage = cartPage.goToCheckoutLoggedIn();
            assertDeliveryAndInvoiceAddressesMatch(checkoutPage);
            CreditCardDetailsData cardData = CreditCardDetailsDataFactory.validCreditCardDetails();
            OrderPlacedPage orderPlacedPage = flows.placeOrderFromCheckoutAsLoggedInUser(checkoutPage, ORDER_COMMENT, cardData);
            Assert.assertEquals(orderPlacedPage.getOrderSuccessMessage(), ORDER_SUCCESS_MESSAGE);
            homePage = orderPlacedPage.continueShopping();
            TestAssertions.deleteAccountAndAssertHomePage(homePage);
            homePage = null;
        } finally {
            deleteAccountIfPossible(homePage);
        }
    }

    @Test(groups = {"regression", "order", "checkout", "data_integrity", "destructive", "slow"})
    public void userCanPlaceOrderAfterRegisteringAndAddingProducts() {
        HomePage homePage = null;

        try {
            CreateAccountPage createAccountPage = beginUserRegistration(testUser.getIdentity());
            AccountCreatedPage accountCreatedPage = createAccountPage.registerAccount(testUser.getProfile());
            homePage = accountCreatedPage.continueToHomePage();
            TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());

            ProductsPage productsPage = homePage.getNavBar().navigateToProducts();
            CartPage cartPage = flows.addProductsAndGoToCart(productsPage, 2);
            CheckoutPage checkoutPage = cartPage.goToCheckoutLoggedIn();
            assertDeliveryAndInvoiceAddressesMatch(checkoutPage);
            CreditCardDetailsData cardData = CreditCardDetailsDataFactory.additionalValidCreditCardDetails();
            OrderPlacedPage orderPlacedPage = flows.placeOrderFromCheckoutAsLoggedInUser(checkoutPage, ORDER_COMMENT, cardData);
            Assert.assertEquals(orderPlacedPage.getOrderSuccessMessage(), ORDER_SUCCESS_MESSAGE);
            homePage = orderPlacedPage.continueShopping();
            TestAssertions.deleteAccountAndAssertHomePage(homePage);
            homePage = null;
        } finally {
            deleteAccountIfPossible(homePage);
        }
    }

    @Test(groups = {"smoke", "regression", "order", "checkout", "critical_path", "slow"})
    public void userCanPlaceAnOrderAfterLoginToExistingAccount() {
        UserIdentityData userDataExistingUser = UserIdentityDataFactory.existingSeededUser();
        HomePage homePage = loginAsExistingUser(userDataExistingUser);
        TestAssertions.assertLoggedInUser(homePage, userDataExistingUser);

        ProductsPage productsPage = homePage.getNavBar().navigateToProducts();
        CartPage cartPage = flows.addProductsAndGoToCart(productsPage, 2);
        CheckoutPage checkoutPage = cartPage.goToCheckoutLoggedIn();
        assertDeliveryAndInvoiceAddressesMatch(checkoutPage);
        CreditCardDetailsData cardData = CreditCardDetailsDataFactory.additionalValidCreditCardDetails();
        OrderPlacedPage orderPlacedPage = flows.placeOrderFromCheckoutAsLoggedInUser(checkoutPage, ORDER_COMMENT, cardData);
        Assert.assertEquals(orderPlacedPage.getOrderSuccessMessage(), ORDER_SUCCESS_MESSAGE);
    }

    @Test(groups = {"regression", "order", "checkout", "download", "data_integrity", "destructive", "slow"})
    public void userCanDownloadInvoiceAfterPlacingOrder() {
        HomePage homePage = null;
        AccountRegistrationData regData = AccountRegistrationTestDataFactory.validRegistrationUserFemale();
        UserIdentityData userData = UserIdentityDataFactory.newUniqueUser();
        testUser = new TestUser(regData, userData);
        String path = ConfigReader.getProperty("downloadDir");
        String absolutePath = Paths.get(path).toAbsolutePath().toString();
        Path downloadPath = Paths.get(absolutePath);
        ProductsPage productsPage = openProductsPage();
        CartPage cartPage = flows.addProductsAndGoToCart(productsPage, 1);

        try {
            LoginPage loginPage = cartPage.goToLoginFromCheckout();
            homePage = flows.registerAndContinueToHomePage(loginPage, testUser);
            TestAssertions.assertLoggedInUser(homePage, testUser.getIdentity());

            cartPage = homePage.getNavBar().navigateToCart();
            CheckoutPage checkoutPage = cartPage.goToCheckoutLoggedIn();
            assertDeliveryAndInvoiceAddressesMatch(checkoutPage);
            CreditCardDetailsData cardData = CreditCardDetailsDataFactory.validCreditCardDetails();
            OrderPlacedPage orderPlacedPage = flows.placeOrderFromCheckoutAsLoggedInUser(checkoutPage, INVOICE_ORDER_COMMENT, cardData);
            Assert.assertEquals(orderPlacedPage.getOrderSuccessMessage(), ORDER_SUCCESS_MESSAGE);
            Instant downloadStartedAt = Instant.now();
            orderPlacedPage.downloadInvoice();
            File invoice = FileDownloadUtils.waitForDownloadedFile(
                    downloadPath,
                    "invoice",
                    Duration.ofSeconds(10),
                    downloadStartedAt
            );
            FileDownloadUtils.assertValidDownloadedFile(invoice, "invoice");
            homePage = orderPlacedPage.continueShopping();
            TestAssertions.deleteAccountAndAssertHomePage(homePage);
            homePage = null;
        } finally {
            deleteAccountIfPossible(homePage);
        }
    }
}
