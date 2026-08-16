package tests.products;

import constants.Product;
import framework.base.BaseTest;
import framework.helpers.AccountCleanupHelper;
import framework.utils.TestAssertions;
import framework.utils.TextNormalizer;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import models.CartItem;
import models.ProductDetails;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;

public class ProductTests extends BaseTest {
    private static final String MEN_CATEGORY = "Men";
    private static final String JEANS_SUBCATEGORY = "Jeans";
    private static final String WOMEN_CATEGORY = "Women";
    private static final String SAREE_SUBCATEGORY = "Saree";
    private static final String POLO_BRAND = "Polo";
    private static final String SEARCH_PRODUCTS_VALUE = "Tshirt";
    private static final String REVIEW_SUCCESS_MESSAGE = "Thank you for your review.";
    private static final String EMPTY_CART_MESSAGE = "Cart is empty!";

    @Test(groups = {"smoke", "regression", "products", "non_destructive", "fast"})
    public void userCanViewProductDetailsFromProductsPage() {
        String productName = Product.MEN_TSHIRT.getProductName();
        String productCategory = Product.MEN_TSHIRT.getCategories();
        BigDecimal productPrice = Product.MEN_TSHIRT.getProductPrice();
        String productCondition = Product.MEN_TSHIRT.getCondition();
        String productAvailability = Product.MEN_TSHIRT.getAvailability();
        String productBrand = Product.MEN_TSHIRT.getBrandName();

        ProductsPage productsPage = flows.openProductsPage();
        ProductDetailsPage productDetailsPage = productsPage.viewSingleProduct(productName);
        ProductDetails actual = productDetailsPage.getProductDetails();

        Assert.assertEquals(actual.getName(), productName);
        Assert.assertEquals(actual.getCategory(), productCategory);
        Assert.assertEquals(
                actual.getPrice().compareTo(productPrice), 0, "Actual and expected product price are not the same. ");
        Assert.assertEquals(actual.getAvailability(), productAvailability);
        Assert.assertEquals(actual.getCondition(), productCondition);
        Assert.assertEquals(actual.getBrand(), productBrand);
    }

    @Test(groups = {"smoke", "regression", "products", "non_destructive", "fast"})
    public void userCanSearchForProductByName() {
        String productName = Product.BLUE_TOP.getProductName();
        ProductsPage productsPage = flows.openProductsPage();
        productsPage.searchProduct(productName);
        Assert.assertEquals(productsPage.getFirstSearchProductName(), productName);
    }

    @Test(groups = {"regression", "products", "cart", "data_integrity", "slow"})
    public void userCanAddMultipleProductsToCart() {
        List<String> testProducts =
                Arrays.asList(Product.BLUE_TOP.getProductName(), Product.MEN_TSHIRT.getProductName());

        ProductsPage productsPage = flows.openProductsPage();
        CartPage cartPage = flows.addProductsAndGoToCart(productsPage, testProducts.size());
        // Assert size, contents and price for each item
        List<CartItem> items = cartPage.readCartItems();
        Assert.assertEquals(items.size(), testProducts.size());
        List<String> names = items.stream().map(CartItem::getProductName).toList();
        Assert.assertEquals(names, testProducts);

        for (CartItem item : items) {
            BigDecimal expectedTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            Assert.assertEquals(
                    item.getTotalPrice(), expectedTotal, "Total price mismatch for product: " + item.getProductName());
        }
    }

    @Test(groups = {"regression", "products", "cart", "data_integrity", "slow"})
    public void userCanAddMultipleQuantitiesOfProductToCart() {
        int productQuantity = 4;
        String productName = Product.SLEEVELESS_DRESS.getProductName();
        int productCount = 1;

        ProductsPage productsPage = flows.openProductsPage();
        ProductDetailsPage productDetailsPage = productsPage.viewSingleProduct(productName);
        productDetailsPage.setProductQuantity(productQuantity);
        CartPage cartPage = productDetailsPage.addToCartAndGoToCart();
        List<CartItem> items = cartPage.readCartItems();
        Assert.assertEquals(items.size(), productCount);
        for (CartItem item : items) {
            Assert.assertEquals(item.getProductName(), productName);
            Assert.assertEquals(item.getQuantity(), productQuantity);
        }
    }

    @Test(groups = {"regression", "cart", "data_integrity", "fast"})
    public void userCanAddAndRemoveProductsFromCart() {
        ProductsPage productsPage = flows.openProductsPage();
        CartPage cartPage = flows.addProductsAndGoToCart(productsPage, 3);
        cartPage.clearCart();
        Assert.assertEquals(cartPage.getEmptyCartMessage(), EMPTY_CART_MESSAGE);
    }

    @Test(groups = {"regression", "products", "navigation", "non_destructive", "fast"})
    public void userCanBrowseProductsByCategory() {
        HomePage homePage = flows.openHomePage();

        ProductsPage productsPage = homePage.category.selectCategoryAndSubcategory(MEN_CATEGORY, JEANS_SUBCATEGORY);
        Assert.assertEquals(productsPage.getProductResultTitle(), (JEANS_SUBCATEGORY));
        productsPage = productsPage.category.selectCategoryAndSubcategory(WOMEN_CATEGORY, SAREE_SUBCATEGORY);
        Assert.assertEquals(productsPage.getProductResultTitle(), (SAREE_SUBCATEGORY));
    }

    @Test(groups = {"regression", "products", "non_destructive", "fast"})
    public void userCanFilterProductsByBrand() {
        HomePage homePage = flows.openHomePage();
        ProductsPage productsPage = homePage.brand.selectProductBrandByName(POLO_BRAND);
        Assert.assertEquals(productsPage.getProductResultTitle(), POLO_BRAND);
    }

    @Test(groups = {"regression", "products", "cart", "auth", "critical_path", "destructive", "slow"})
    public void userCartIsPreservedAfterLogin() {
        HomePage homePage = null;
        try {
            // Provision a fresh account, then browse anonymously before logging in.
            LoginPage provisionedLoginPage = flows.registerAndLogOut(testUser);
            ProductsPage productsPage = provisionedLoginPage.getNavBar().navigateToProducts();
            productsPage.searchProduct(SEARCH_PRODUCTS_VALUE);
            Set<String> searchResults = productsPage.getAllSearchProductNames();
            CartPage cartPage = flows.addAllProductsAndGoToCart(productsPage);
            List<CartItem> items = cartPage.readCartItems();
            Set<String> cartProductNames = items.stream()
                    .map(CartItem::getProductName)
                    .map(TextNormalizer::normalizeText)
                    .collect(Collectors.toSet());
            Assert.assertEquals(cartProductNames, searchResults);

            LoginPage loginPage = cartPage.getNavBar().navigateToLogin();
            homePage = loginPage.logInAccount(testUser.getIdentity());
            cartPage = homePage.getNavBar().navigateToCart();
            List<CartItem> itemsAfterLogin = cartPage.readCartItems();
            Set<String> cartProductNamesAfterLogin = itemsAfterLogin.stream()
                    .map(CartItem::getProductName)
                    .map(TextNormalizer::normalizeText)
                    .collect(Collectors.toSet());
            Assert.assertEquals(cartProductNamesAfterLogin, searchResults);

            TestAssertions.deleteAccountAndAssertHomePage(homePage);
            homePage = null;
        } finally {
            AccountCleanupHelper.deleteAccountIfPossible(homePage);
        }
    }

    @Test(groups = {"regression", "products", "destructive", "slow"})
    public void userCanAddReviewToProduct() {
        String productName = Product.SLEEVELESS_DRESS.getProductName();
        ProductsPage productsPage = flows.openProductsPage();
        ProductDetailsPage productDetailsPage = productsPage.viewSingleProduct(productName);
        productDetailsPage.addReviewToProduct("Reviewer", "test@review.com", "Good product quality");
        Assert.assertEquals(productDetailsPage.getReviewSubmissionConfirmation(), REVIEW_SUCCESS_MESSAGE);
    }

    @Test(groups = {"regression", "products", "cart", "fast"})
    public void userCanAddRecommendedProductToCart() {
        String productName = Product.SLEEVELESS_DRESS.getProductName();
        HomePage homePage = flows.openHomePage();
        homePage.addRecommendedProductToCart(productName);
        CartPage cartPage = homePage.modal.goToCart();
        Assert.assertEquals(
                TextNormalizer.normalizeText(cartPage.getCartItemName()), TextNormalizer.normalizeText(productName));
    }
}
