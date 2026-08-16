package components;

import framework.drivers.DriverContext;
import framework.exceptions.ElementNotFoundException;
import framework.utils.TextNormalizer;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pages.ProductsPage;

public class BrandFilterComponent extends BaseComponent {
    public BrandFilterComponent(DriverContext driverContext) {
        super(driverContext);
    }

    private final By brandsFilter = By.className("brands-name");
    private final By brandsList = By.xpath("//ul[@class='nav nav-pills nav-stacked']/li");

    public ProductsPage selectProductBrandByName(String brandName) {
        List<WebElement> brands = driver.findElements(brandsList);
        String normalizedTarget = TextNormalizer.normalizeText(brandName);
        WebElement selectedBrand = brands.stream()
                .filter(b -> TextNormalizer.normalizeText(b.getText()).equals(normalizedTarget))
                .findFirst()
                .orElseThrow(() -> new ElementNotFoundException("Brand " + normalizedTarget + " not found."));
        waitAndScrollToElement(brandsFilter);
        click(selectedBrand);
        return new ProductsPage(driverContext);
    }
}
