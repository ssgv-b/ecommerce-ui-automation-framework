package components;

import framework.drivers.DriverContext;
import framework.utils.TextNormalizer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pages.ProductsPage;

import java.util.List;

public class BrandFilterComponent extends BaseComponent{
    public BrandFilterComponent (DriverContext driverContext){
        super(driverContext);
    }

    private final By brandsFilter = By.className("brands-name");
    private final By brandsList = By.xpath("//ul[@class='nav nav-pills nav-stacked']/li");

    public ProductsPage selectProductBrandByName(String brandName) {
        List<WebElement> brands = driver.findElements(brandsList);
        String normalizedTarget = TextNormalizer.normalizeText(brandName);
        WebElement selectedBrand = brands.stream().filter(b-> TextNormalizer.normalizeText(b.getText())
                .equals(normalizedTarget)).findFirst().orElseThrow(()->new RuntimeException("Brand "+normalizedTarget+" not found."));
        waitAndScrollToElement(brandsFilter);
        click(selectedBrand);
        return new ProductsPage(driverContext);
    }

}
