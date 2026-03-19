package framework.utils;

import models.AccountRegistrationData;
import models.Address;
import models.UserIdentityData;
import org.testng.Assert;
import pages.AccountDeletedPage;
import pages.CheckoutPage;
import pages.HomePage;

import java.util.Locale;

public class TestAssertions {

    public static void assertDeliveryAndInvoiceAddressesMatch(CheckoutPage checkoutPage) {
        Address deliveryAddress = normalizeAddress(checkoutPage.getDeliveryAddress());
        Address invoiceAddress = normalizeAddress(checkoutPage.getInvoiceAddress());
        Assert.assertEquals(
                deliveryAddress, invoiceAddress,
                "The delivery and invoice addresses don't match.");
    }

    public static void assertRegistrationAndCheckoutAddressesMatch(CheckoutPage checkoutPage, AccountRegistrationData registrationData) {
        Address expectedAddress = normalizeAddress(registrationData.getAddressRegistrationData());
        Address deliveryAddress = normalizeAddress(checkoutPage.getDeliveryAddress());
        Address invoiceAddress = normalizeAddress(checkoutPage.getInvoiceAddress());

        Assert.assertEquals(
                deliveryAddress, expectedAddress,
                "The delivery address doesn't match the registration address.");
        Assert.assertEquals(
                invoiceAddress, expectedAddress,
                "The invoice address doesn't match the registration address.");
    }

    private static Address normalizeAddress(Address address) {
        return new Address(
                normalizeName(address.getName()),
                normalizeValue(address.getStreet()),
                normalizeValue(address.getCityStateZip()),
                normalizeValue(address.getCountry()),
                normalizeValue(address.getPhoneNumber())
        );
    }

    private static String normalizeName(String name) {
        return AddressNormalizer.parseAddressName(name);
    }

    private static String normalizeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public static void assertLoggedInUser(HomePage homePage, UserIdentityData userData) {
        String currentUser = homePage.getCurrentUsername();
        String expectedUser = userData.getUserName();
        Assert.assertEquals(currentUser, expectedUser,
                "Current logged in user and registration user don't match.");
    }

    public static void deleteAccountAndAssertHomePage(HomePage homePage) {
        AccountDeletedPage accountDeletedPage = homePage.getNavBar().navigateToDeleteAccount();
        HomePage returnedHomePage = accountDeletedPage.continueToHomePage();
        returnedHomePage.assertOnHomePage();
    }
}
