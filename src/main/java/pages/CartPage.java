package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import framework.utils.ProductTextParser;
import models.CartItem;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartPage extends BasePage {
    public CartPage(DriverContext driverContext) {
        super(driverContext);
        By cartPageSignal = By.xpath("//section[@id='do_action']");
        waitForVisibleElement(cartPageSignal);
    }

    private final By cartPrice = By.xpath("//td[@class='cart_price']/p");
    private final By cartQuantity = By.xpath("//td[@class='cart_quantity']/button");
    private final By cartTotalPrice = By.xpath("//p[@class='cart_total_price']");
    private final By cartProductName = By.xpath(".//td[@class='cart_description']/h4/a");
    private final By removeFromCartButton = By.className("cart_quantity_delete");
    private final By cartTableRow = By.xpath("//tbody/tr");
    private final By goToCheckoutButton = By.cssSelector("*.check_out");
    private final By registerLogInModalButton = By.xpath("//p[@class='text-center'][2]/a");
    private final By cartEmptyMessage = By.xpath("//b[contains(text(),'Cart is empty!')]");
    private static final String PRICE_CURRENCY_PREFIX = "Rs. ";


    public void clearCart() {
        while (!getCurrentCartItems().isEmpty()) {
            List<WebElement> cartItems = getCurrentCartItems();
            int previousCount = cartItems.size();
            cartItems.getFirst().findElement(removeFromCartButton).click();
            try {
                wait.until(driver -> getCurrentCartItems().size() < previousCount);
            }
            catch (TimeoutException e) {
                throw new RuntimeException("Cart row count did not decrease after remove click", e);
            }
        }
    }

    private List <WebElement> getCurrentCartItems() {
        return driver.findElements(cartTableRow);
    }

    public String getEmptyCartMessage() {
        return getTextWhenVisible(cartEmptyMessage);
    }

    public List <CartItem> readCartItems() {
        waitForVisibleElement(cartTableRow);
        List<WebElement> products = getCurrentCartItems();
        List<CartItem> cartItems = new ArrayList<>();
        for (WebElement product : products) {
            String name = product.findElement(cartProductName).getText();
            // Read price qty and total price and convert to appropriate types
            String priceText = parseProductPricing(cartPrice, product);
            BigDecimal price = ProductTextParser.parseBigDecimal(priceText);
            String quantityText = product.findElement(cartQuantity).getText().trim();
            int quantity = Integer.parseInt(quantityText);
            String totalPriceText = parseProductPricing(cartTotalPrice, product);
            BigDecimal totalPrice = ProductTextParser.parseBigDecimal(totalPriceText);
            CartItem cartItem = new CartItem(name, price, quantity, totalPrice);
            cartItems.add(cartItem);
        }
        return cartItems;
    }

    public String getCartItemName() {
        waitForVisibleElement(cartTableRow);
        List <WebElement> products = getCurrentCartItems();
       return products.stream()
                .map(e->normalizer.normalizeText(e.findElement(cartProductName)
                        .getText()))
                .findFirst()
                .orElseThrow(()->new RuntimeException("No products found in cart!"));
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

    private String parseProductPricing(By locator, WebElement product) {
        return product.findElement(locator).getText().replace(PRICE_CURRENCY_PREFIX,"").trim();
    }
}
