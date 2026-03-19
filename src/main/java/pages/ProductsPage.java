package pages;

import components.BrandFilterComponent;
import components.CategoryFilterComponent;
import framework.base.BasePage;
import framework.drivers.DriverContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductsPage extends BasePage {
    public ProductsPage(DriverContext driverContext) {
        super(driverContext);
        By productsPageSignal = By.cssSelector(".product-image-wrapper");
        waitForVisibleElement(productsPageSignal);
        this.category = new CategoryFilterComponent(driverContext);
        this.brand = new BrandFilterComponent(driverContext);
    }

    private final By productCards = By.className("product-image-wrapper");
    private final By viewProductButton = By.xpath(".//a[contains (text(),'View Product')]");
    private final By productNameLocator = By.cssSelector(".productinfo p");
    private final By searchInput = By.id("search_product");
    private final By searchButton = By.id("submit_search");
    private final By searchedProductsTitle = By.xpath("//h2[contains (text(), 'Searched Products')]");
    private final By continueShoppingButton = By.cssSelector(".btn-success");
    private final By addToCartButton = By.xpath(".//a[contains (text(),'Add to cart')]");
    private final By productsTitle = By.xpath("//h2[@class='title text-center']");
    public final CategoryFilterComponent category;
    public final BrandFilterComponent brand;

    private WebElement findProductCardByName(String productName) {
        waitForVisibleElement(productNameLocator);
        List <WebElement> products = driver.findElements(productCards);
        String normalizedTarget = normalizer.normalizeText(productName);
        return products.stream().filter(p ->
                normalizer.normalizeText(p.findElement(productNameLocator).getText()).equals(normalizedTarget))
                .findFirst().orElseThrow(() -> new RuntimeException("Product name " + productName + " not found!"));
    }

    public void searchProduct(String productName) {
        enterText(searchInput, productName);
        click(searchButton);
        waitForVisibleElement(searchedProductsTitle);
    }

    public void addProductsToCart(int count) {
        List<WebElement> products = driver.findElements(productCards);
        if (count ==0) {
            return;
        }
        if (products.isEmpty()) {
            throw new RuntimeException("No search results found!");
        }
        int actualCount = Math.min(count, products.size());
        for (int i=0; i<actualCount; i++) {
            WebElement product = products.get(i);
            product.findElement(addToCartButton).click();
            waitForVisibleElement(continueShoppingButton);
            click(continueShoppingButton);
        }

    }

    public ProductDetailsPage viewSingleProduct(String productName) {
        WebElement selectedProduct = findProductCardByName(productName);
        selectedProduct.findElement(viewProductButton).click();
        return new ProductDetailsPage(driverContext);
    }

    public String getProductResultTitle() {
        String title = getTextWhenVisible(productsTitle);
        String lowerCaseTitle = title.replaceAll("^.* -", "")
                .replaceAll("(?i) products\\s*$", "")
                .trim()
                .toLowerCase(Locale.ROOT);
        if (lowerCaseTitle.isBlank()) {
            return lowerCaseTitle;
        }
        if (lowerCaseTitle.length() == 1) {
            return lowerCaseTitle.toUpperCase(Locale.ROOT);
        }
        String remainder = lowerCaseTitle.substring(1);
        String capitalLetter = lowerCaseTitle.substring(0,1).toUpperCase(Locale.ROOT);
        return  capitalLetter+remainder;
    }

    public Set<String> getAllSearchProductNames() {
       List <WebElement> results =  driver.findElements(productNameLocator);
       return results.stream().map(WebElement::getText).map(normalizer::normalizeText).collect(Collectors.toSet());
    }

    public String getFirstSearchProductName() {
        List<WebElement> results = driver.findElements(productCards);
        return results.stream()
                .map(e -> e.findElement(productNameLocator).getText())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No search result found!"));
    }


}
