package components;

import framework.drivers.DriverContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pages.CartPage;

public class AddToCartModalComponent extends BaseComponent{
    private final By continueShoppingBtn = By.xpath("//button[@data-dismiss='modal']");
    private final By addToCartModal = By.id("cartModal");
    private final By goToCartBtn = By.xpath("//a[@href='/view_cart']");
    public AddToCartModalComponent(DriverContext driverContext){
        super(driverContext);
    }

    private WebElement scopeToModal() {
        waitForClickable(addToCartModal);
        return driver.findElement(addToCartModal);
    }

    public void continueShopping() {
        scopeToModal().findElement(continueShoppingBtn).click();
    }

    public CartPage goToCart() {
        scopeToModal().findElement(goToCartBtn).click();
        return new CartPage(driverContext);
    }
}
