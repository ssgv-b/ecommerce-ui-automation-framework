package components;

import framework.drivers.DriverContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pages.ProductsPage;

import java.util.List;

public class CategoryFilterComponent extends BaseComponent {
    public CategoryFilterComponent(DriverContext driverContext) {
        super(driverContext);
    }

    private final By categoryExpander = By.xpath(".//a[@data-parent='#accordian']/span");
    private final By categoryList = By.xpath("//div[@class='panel-heading']");
    private final By subCategoryList = By.xpath("//div[@class='panel-collapse in']/div/ul/li/a");

    public ProductsPage selectCategoryAndSubcategory(String mainCategory, String subCategory) {
        WebElement category = selectMainCategory(mainCategory);
        ensureExpandedCategory(category);
        selectSubCategory(category, subCategory);
        return new ProductsPage(driverContext);
    }

    private void ensureExpandedCategory(WebElement selectedCategory) {
        if (!selectedCategory.findElements(subCategoryList).isEmpty())
            return;
        WebElement expander = selectedCategory.findElement(categoryExpander);
        click(expander);
        wait.until(driver -> !selectedCategory.findElements(subCategoryList).isEmpty());
    }


    private WebElement selectMainCategory(String mainCategory) {
        List<WebElement> categoryElements = driver.findElements(categoryList);
        return categoryElements.stream().filter(category ->
                        category.getText().equalsIgnoreCase(mainCategory)).findFirst()
                .orElseThrow(() -> new RuntimeException("Category " + mainCategory + " not found."));
    }

    private void selectSubCategory(WebElement selectedCategory, String subCategory) {
        List<WebElement> subCategoryElements = selectedCategory.findElements(subCategoryList);
        WebElement selectedSubCategory = subCategoryElements.stream().filter(category ->
                category.getText().trim().equalsIgnoreCase(subCategory)).findFirst().orElseThrow(() -> new RuntimeException("Subcategory " + subCategory + " not found."));
        click(selectedSubCategory);
    }

}
