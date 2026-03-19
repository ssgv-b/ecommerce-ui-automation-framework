package pages;

import components.AddToCartModalComponent;
import components.BrandFilterComponent;
import framework.drivers.DriverContext;
import framework.base.BasePage;
import framework.utils.ConfigReader;
import models.UserIdentityData;
import org.openqa.selenium.*;
import org.testng.Assert;
import components.CategoryFilterComponent;

import java.util.List;

public class HomePage extends BasePage {

    private final By homePageIdentifier = By.id("slider");
    private final By recommendedItemsSection = By.id("recommended-item-carousel");
    private final By carouselProductWrapper = By.cssSelector(".product-image-wrapper");
    private final By recommendedItemsTitle = By.xpath("//div[@class='recommended_items']/h2");
    private final By carouselProductName = By.cssSelector(".productinfo p");
    private final By carouselAddToCartBtn = By.cssSelector(".btn.btn-default.add-to-cart");
    private final By acceptCookiesButton = By.xpath("//button[@aria-label='Consent']");
    private final By loggedInAsUsername = By.xpath("//a[contains(., 'Logged in as')]");
    private final String BASE_URL = ConfigReader.getProperty("baseUrl");

    public final CategoryFilterComponent category;
    public final BrandFilterComponent brand;
    public final AddToCartModalComponent modal;

    public HomePage(DriverContext driverContext) {
        super(driverContext);
        this.category = new CategoryFilterComponent(driverContext);
        this.brand = new BrandFilterComponent(driverContext);
        this.modal = new AddToCartModalComponent(driverContext);
    }

    public void assertOnHomePage() {
        waitForVisibleElement(homePageIdentifier);
        acceptCookiesIfPresent();
        Assert.assertTrue(driver.getCurrentUrl().contains(BASE_URL));
    }

    public String getCurrentUsername() {
       String fullUserText =  getTextWhenVisible(loggedInAsUsername);
        return fullUserText.replace("Logged in as ", "").trim();
    }

    private void goToCarouselSection() {
        waitAndScrollToElement(recommendedItemsTitle);
        waitForVisibleElement(recommendedItemsSection);
    }

    private WebElement getCarouselProductName(String productName) {
        List<WebElement> carouselItems = driver.findElements(carouselProductWrapper);
        String normalizedTarget = normalizer.normalizeText(productName);
         return carouselItems.stream().filter(c->normalizer.normalizeText(c.findElement(carouselProductName)
                .getText()).equals(normalizedTarget)).findFirst()
                .orElseThrow(()->new RuntimeException("Carousel with name "+productName+" not found!"));
    }
    public void addRecommendedProductToCart(String productName) {
        goToCarouselSection();
        WebElement carouselProduct = getCarouselProductName(productName);
        carouselProduct.findElement(carouselAddToCartBtn).click();
    }

    public void scrollToHeroSection() {
        waitAndScrollToElement(homePageIdentifier);
    }

    private void acceptCookiesIfPresent() {
        try {
            waitForVisibleElement(acceptCookiesButton);
            click(acceptCookiesButton);
        } catch (NoSuchElementException | TimeoutException ignored) {
            // If the cookie consent button is not found, do nothing
        }
    }
}
