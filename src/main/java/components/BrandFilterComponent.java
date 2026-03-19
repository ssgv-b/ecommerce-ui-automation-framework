package components;

import framework.drivers.DriverContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pages.ProductsPage;

import java.util.List;

public class BrandFilterComponent extends BaseComponent{
    public BrandFilterComponent (DriverContext driverContext){
        super(driverContext);

    }

    private By brandsFilter = By.className("brands-name");
    private By brandsList = By.xpath("//ul[@class='nav nav-pills nav-stacked']/li");

    public ProductsPage selectProductBrandByName(String brandName) {
        List<WebElement> brands = driver.findElements(brandsList);
        String normalizedTarget = normalizer.normalizeText(brandName);
        WebElement selectedBrand = brands.stream().filter(b-> normalizer.normalizeText(b.getText())
                .equals(normalizedTarget)).findFirst().orElseThrow(()->new RuntimeException("Brand "+normalizedTarget+" not found."));
        waitAndScrollToElement(brandsFilter);
        selectedBrand.click();
        return new ProductsPage(driverContext);
    }

}
