package flows;

import framework.drivers.DriverContext;
import models.AccountRegistrationData;
import models.CreditCardDetailsData;
import models.TestUser;
import models.UserIdentityData;
import pages.*;

public class TestFlows {
    private final DriverContext driverContext;
    public TestFlows(DriverContext driverContext) {
        this.driverContext = driverContext;
    }

    public CartPage addProductsAndGoToCart(ProductsPage productsPage, int count) {
        productsPage.addProductsToCart(count);
        return productsPage.getNavBar().navigateToCart();
    }

    public OrderPlacedPage placeOrderFromCheckoutAsLoggedInUser(CheckoutPage checkoutPage, String comment, CreditCardDetailsData data) {
        checkoutPage.enterOrderComment(comment);
        PaymentPage paymentPage = checkoutPage.placeOrder();
        return paymentPage.enterPaymentDetailsAndPlaceOrder(data);
    }

    public LoginPage createMinimalAccountAndLogOut(CreateAccountPage createAccountPage,
                                         AccountRegistrationData registrationData) {
        AccountCreatedPage accountCreatedPage = createAccountPage.registerAccount(registrationData);
        HomePage homePage = accountCreatedPage.continueToHomePage();
        return homePage.getNavBar().logOut();
    }

    public HomePage registerAndContinueToHomePage(LoginPage loginPage, TestUser testUser) {
        CreateAccountPage createAccountPage = loginPage.createAccount(testUser.getIdentity());
        AccountCreatedPage accountCreatedPage = createAccountPage.registerAccount(testUser.getProfile());
        return accountCreatedPage.continueToHomePage();
    }

    public HomePage openHomePage() {
        HomePage homePage = new HomePage(driverContext);
        homePage.assertOnHomePage();
        return homePage;
    }

    public ProductsPage openProductsPage() {
        HomePage homePage = openHomePage();
        return homePage.getNavBar().navigateToProducts();
    }

    public HomePage loginAsExistingUser(UserIdentityData identity) {
        LoginPage loginPage = openLoginPage();
        return loginPage.logInAccount(identity);
    }

    public CreateAccountPage beginUserRegistration(UserIdentityData identity) {
        LoginPage loginPage = openLoginPage();
        return loginPage.createAccount(identity);
    }

    public LoginPage openLoginPage() {
        HomePage homePage = openHomePage();
        return homePage.getNavBar().navigateToLogin();
    }
}
