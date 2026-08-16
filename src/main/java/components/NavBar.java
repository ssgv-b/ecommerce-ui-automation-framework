package components;

import framework.drivers.DriverContext;
import org.openqa.selenium.By;
import pages.*;

public class NavBar extends BaseComponent {

    public NavBar(DriverContext driverContext) {
        super(driverContext);
    }

    private final By cartLink = By.xpath("//a[@href='/view_cart']");
    private final By homeLink = By.xpath("//a[normalize-space()='Home']");
    private final By productsLink = By.xpath("//a[@href='/products']");
    private final By signUpLoginLink = By.xpath("//a[@href='/login']");
    private final By contactUsLink = By.xpath("//a[@href='/contact_us']");
    private final By deleteAccountLink = By.cssSelector("a[href='/delete_account']");
    private final By testCasesLink = By.xpath("//a[@href='/test_cases']");
    private final By logoutLink = By.cssSelector("a[href='/logout']");

    public HomePage navigateToHome() {
        click(homeLink);
        return new HomePage(driverContext);
    }

    public CartPage navigateToCart() {
        click(cartLink);
        return new CartPage(driverContext);
    }

    public ProductsPage navigateToProducts() {
        click(productsLink);
        return new ProductsPage(driverContext);
    }

    public LoginPage navigateToLogin() {
        click(signUpLoginLink);
        return new LoginPage(driverContext);
    }

    public AccountDeletedPage navigateToDeleteAccount() {
        click(deleteAccountLink);
        return new AccountDeletedPage(driverContext);
    }

    public ContactPage navigateToContactUs() {
        click(contactUsLink);
        return new ContactPage(driverContext);
    }

    public TestCasesPage navigateToTestCases() {
        click(testCasesLink);
        return new TestCasesPage(driverContext);
    }

    public LoginPage logOut() {
        click(logoutLink);
        return new LoginPage(driverContext);
    }
}
