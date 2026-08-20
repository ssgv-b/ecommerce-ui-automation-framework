package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import framework.exceptions.ElementNotFoundException;
import framework.exceptions.PageStateException;
import framework.utils.ProductTextParser;
import framework.utils.TextNormalizer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import models.CartItem;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

public class CartPage extends BasePage {
    public CartPage(DriverContext driverContext) {
        super(driverContext);
        By cartPageSignal = By.xpath("//section[@id='cart_items']");
        waitForVisibleElement(cartPageSignal);
    }

    private final By cartPrice = By.xpath(".//td[@class='cart_price']/p");
    private final By cartQuantity = By.xpath(".//td[@class='cart_quantity']/button");
    private final By cartTotalPrice = By.xpath(".//p[@class='cart_total_price']");
    private final By cartProductName = By.xpath(".//td[@class='cart_description']/h4/a");
    private final By removeFromCartButton = By.className("cart_quantity_delete");
    private final By cartTableRow = By.xpath("//tbody/tr");
    private final By goToCheckoutButton = By.cssSelector("*.check_out");
    private final By registerLogInModalButton = By.xpath("//p[@class='text-center'][2]/a");
    private final By cartEmptyMessage = By.cssSelector("#empty_cart b");

    public void clearCart() {
        while (!getCurrentCartRows().isEmpty()) {
            List<WebElement> cartItems = getCurrentCartRows();
            int previousCount = cartItems.size();
            cartItems.getFirst().findElement(removeFromCartButton).click();
            try {
                wait.until(driver -> getCurrentCartRows().size() < previousCount);
            } catch (TimeoutException e) {
                throw new PageStateException("Cart row count did not decrease after remove click", e);
            }
        }
    }

    private List<WebElement> getCurrentCartRows() {
        return driver.findElements(cartTableRow);
    }

    private List<WebElement> waitForCartRows() {
        return waitForElements(cartTableRow);
    }

    public String getEmptyCartMessage() {
        return getTextWhenVisible(cartEmptyMessage);
    }

    public List<CartItem> readCartItems() {
        List<WebElement> cartRows = waitForCartRows();
        List<CartItem> cartItems = new ArrayList<>();
        for (WebElement cartRow : cartRows) {
            String name = cartRow.findElement(cartProductName).getText();
            BigDecimal price =
                    ProductTextParser.parsePrice(cartRow.findElement(cartPrice).getText());
            int quantity =
                    Integer.parseInt(cartRow.findElement(cartQuantity).getText().trim());
            BigDecimal totalPrice = ProductTextParser.parsePrice(
                    cartRow.findElement(cartTotalPrice).getText());

            CartItem cartItem = new CartItem(name, price, quantity, totalPrice);
            cartItems.add(cartItem);
        }
        return cartItems;
    }

    public String getCartItemName() {
        List<WebElement> products = waitForCartRows();
        return products.stream()
                .map(e -> TextNormalizer.normalizeText(
                        e.findElement(cartProductName).getText()))
                .findFirst()
                .orElseThrow(() -> new ElementNotFoundException("No products found in cart!"));
    }

    public CheckoutPage goToCheckoutLoggedIn() {
        click(goToCheckoutButton);
        return new CheckoutPage(driverContext);
    }

    public LoginPage goToLoginFromCheckout() {
        waitForVisibleElement(goToCheckoutButton);
        click(goToCheckoutButton);
        waitForVisibleElement(registerLogInModalButton);
        click(registerLogInModalButton);
        return new LoginPage(driverContext);
    }
}
