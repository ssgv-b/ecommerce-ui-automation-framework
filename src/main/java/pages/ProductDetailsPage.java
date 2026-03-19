package pages;

import framework.base.BasePage;
import framework.drivers.DriverContext;
import framework.utils.ProductTextParser;
import models.ProductDetails;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ProductDetailsPage extends BasePage {
    public ProductDetailsPage(DriverContext driverContext) {
        super(driverContext);
        By productDetailsPageSignal = By.className("product-details");
        waitForVisibleElement(productDetailsPageSignal);
    }
    private final By productName = By.xpath("//div[@class='product-information']/h2");
    private final By productCategory = By.xpath("//p[contains(.,'Category')]");
    private final By productPrice = By.xpath("//div[@class='product-information']/span/span");
    private final By productAvailability = By.xpath("//p[contains(.,'Availability')]");
    private final By productCondition = By.xpath("//p[contains(.,'Condition')]");
    private final By productBrand = By.xpath("//p[contains(.,'Brand')]");
    private final By productQuantityInput = By.id("quantity");
    private final By addToCartButton = By.xpath("//button[@type='button']");
    private final By viewCartButtonModal = By.xpath("//u[contains(text(),'View Cart')]");
    private final By reviewNameInput = By.id("name");
    private final By reviewEmailInput = By.id("email");
    private final By reviewTextInput = By.id("review");
    private final By submitReviewButton = By.id("button-review");
    private final By reviewConfirmationText = By.xpath("//div[@class='alert-success alert']/span");


    public ProductDetails getProductDetails() {
        waitForVisibleElement(productName);
        String name = getTextWhenVisible(productName);
        String category = ProductTextParser.parseTextAndTrim(getTextWhenVisible(productCategory));
        String price = getTextWhenVisible(productPrice).trim();
        String availability = ProductTextParser.parseTextAndTrim(getTextWhenVisible(productAvailability));
        String condition = ProductTextParser.parseTextAndTrim(getTextWhenVisible(productCondition));
        String brand = ProductTextParser.parseTextAndTrim(getTextWhenVisible(productBrand));
        return new ProductDetails(name, category, price, availability, condition, brand);

    }

    public void setProductQuantity(Integer quantity) {
        WebElement element = waitForVisibleElement(productQuantityInput);
        element.clear();
        enterText(productQuantityInput, quantity.toString());
    }

    public CartPage addToCartAndGoToCart() {
        click(addToCartButton);
        waitForVisibleElement(viewCartButtonModal);
        click(viewCartButtonModal);
        return new CartPage(driverContext);
    }

    public void addReviewToProduct(String reviewerName, String reviewerEmail, String reviewerTextInput) {
        enterText(reviewNameInput, reviewerName);
        enterText(reviewEmailInput, reviewerEmail);
        enterText(reviewTextInput, reviewerTextInput);
        click(submitReviewButton);
    }

    public String getReviewSubmissionConfirmation() {
        return getTextWhenVisible(reviewConfirmationText);
    }

}
