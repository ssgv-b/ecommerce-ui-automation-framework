package components;

import framework.drivers.DriverContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.*;

public class NavBar extends BaseComponent{

    public NavBar(DriverContext driverContext){
        super(driverContext);
    }

    private By cartLink = By.xpath("//a[normalize-space()='Cart']");
    private By homeLink = By.xpath("//a[normalize-space()='Home']");
    private By productsLink = By.xpath("//a[@href='/products']");
    private By signUpLoginLink = By.xpath("//a[@href='/login']");
    private By contactUsLink = By.xpath("//a[@href='/contact_us']");
    private By deleteAccountLink = By.cssSelector("a[href='/delete_account']");
    private By testCasesLink = By.xpath("//a[@href='/test_cases']");
    private By logoutLink = By.cssSelector("a[href='/logout']");

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
