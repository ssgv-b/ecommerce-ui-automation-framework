package tests.products;


import framework.base.BaseTest;
import framework.testdata.UserIdentityDataFactory;
import framework.utils.TextNormalizer;
import models.CartItem;
import models.ProductDetails;
import models.UserIdentityData;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductTests extends BaseTest {
    private static final String MEN_TSHIRT = "Men Tshirt";
    private static final String BLUE_TOP = "Blue Top";
    private static final String SLEEVELESS_DRESS = "Sleeveless Dress";
    private static final String MEN_CATEGORY = "Men";
    private static final String JEANS_SUBCATEGORY = "Jeans";
    private static final String WOMEN_CATEGORY = "Women";
    private static final String SAREE_SUBCATEGORY = "Saree";
    private static final String POLO_BRAND = "Polo";
    private static final String BIBA_BRAND = "Biba";
    private static final String SEARCH_PRODUCTS_VALUE = "Tshirt";
    private static final String REVIEW_SUCCESS_MESSAGE = "Thank you for your review.";
    private static final String EMPTY_CART_MESSAGE = "Cart is empty!";
    private final TextNormalizer normalizer = new TextNormalizer();

    @Test(groups = {"smoke", "regression", "products", "non_destructive", "fast"})
    public void userCanViewProductDetailsFromProductsPage() {
        ProductsPage productsPage = openProductsPage();
        ProductDetailsPage productDetailsPage =  productsPage.viewSingleProduct(MEN_TSHIRT);
        ProductDetails actual = productDetailsPage.getProductDetails();

        Assert.assertEquals(actual.getName(), MEN_TSHIRT);
        Assert.assertEquals(actual.getCategory(), "Men > Tshirts");
        Assert.assertEquals(actual.getPrice(), "Rs. 400");
        Assert.assertEquals(actual.getAvailability(), "In Stock");
        Assert.assertEquals(actual.getCondition(), "New");
        Assert.assertEquals(actual.getBrand(), "H&M");
    }

    @Test(groups = {"smoke", "regression", "products", "non_destructive", "fast"})
    public void userCanSearchForProductByName() {
        ProductsPage productsPage = openProductsPage();
        productsPage.searchProduct(BLUE_TOP);
        Assert.assertEquals(productsPage.getFirstSearchProductName(),BLUE_TOP);
    }

    @Test(groups = {"regression", "products", "cart", "data_integrity", "slow"})
    public void userCanAddMultipleProductsToCart() {
        ProductsPage productsPage = openProductsPage();
        CartPage cartPage = flows.addProductsAndGoToCart(productsPage,2);
        List<CartItem> items = cartPage.readCartItems();
        Assert.assertEquals(items.size(), 2);
        List<String> names = items.stream().map(CartItem::getProductName).collect(Collectors.toList());
        Assert.assertTrue(names.contains(BLUE_TOP));
        Assert.assertTrue(names.contains(MEN_TSHIRT));

        for (CartItem item : items) {
            BigDecimal expectedTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            Assert.assertEquals(item.getTotalPrice(), expectedTotal,"Total price mismatch for product: " + item.getProductName());
        }
    }

    @Test(groups = {"regression", "products", "cart", "data_integrity", "slow"})
    public void userCanAddMultipleQuantitiesOfProductToCart() {
        Integer productQuantity = 4;
        ProductsPage productsPage = openProductsPage();
        ProductDetailsPage productDetailsPage =  productsPage.viewSingleProduct(SLEEVELESS_DRESS);
        productDetailsPage.setProductQuantity(productQuantity);
        CartPage cartPage = productDetailsPage.addToCartAndGoToCart();
        List<CartItem> items = cartPage.readCartItems();
        Assert.assertEquals(items.size(), 1);
        for (CartItem item : items) {
            Assert.assertEquals(item.getProductName(), SLEEVELESS_DRESS);
            Assert.assertEquals(item.getQuantity(), productQuantity);
        }
    }
    @Test(groups = {"regression", "cart", "data_integrity", "fast"})
    public void userCanAddAndRemoveProductsFromCart() {
        ProductsPage productsPage = openProductsPage();
        CartPage cartPage = flows.addProductsAndGoToCart(productsPage, 3);
        cartPage.clearCart();
        Assert.assertEquals(cartPage.getEmptyCartMessage(), EMPTY_CART_MESSAGE);
    }

    @Test(groups = {"regression", "products", "navigation", "non_destructive", "fast"})
    public void userCanBrowseProductsByCategory() {
        HomePage homePage = openHomePage();

        ProductsPage productsPage = homePage.category.selectCategoryAndSubcategory(MEN_CATEGORY, JEANS_SUBCATEGORY);
        Assert.assertEquals(productsPage.getProductResultTitle(),(JEANS_SUBCATEGORY));
        productsPage = productsPage.category.selectCategoryAndSubcategory(WOMEN_CATEGORY, SAREE_SUBCATEGORY);
        Assert.assertEquals(productsPage.getProductResultTitle(),(SAREE_SUBCATEGORY));
    }

    @Test(groups = {"regression", "products", "non_destructive", "fast"})
    public void userCanFilterProductsByBrand() {
        HomePage homePage = openHomePage();
        ProductsPage productsPage = homePage.brand.selectProductBrandByName(POLO_BRAND);
        Assert.assertEquals(productsPage.getProductResultTitle(),POLO_BRAND);
    }

    @Test(groups = {"regression", "products", "cart", "auth", "critical_path", "slow"})
    public void userCartIsPreservedAfterLogin() {
        ProductsPage productsPage = openProductsPage();
        productsPage.searchProduct(SEARCH_PRODUCTS_VALUE);
        Set <String> searchResults = productsPage.getAllSearchProductNames();
        CartPage cartPage = flows.addProductsAndGoToCart(productsPage, 5);
        List <CartItem> items = cartPage.readCartItems();
        Set<String> cartProductNames = items.stream()
                .map(CartItem::getProductName)
                .map(normalizer::normalizeText)
                .collect(Collectors.toSet());
        Assert.assertEquals(cartProductNames,searchResults);

        LoginPage loginPage = cartPage.getNavBar().navigateToLogin();
        UserIdentityData userDataExistingUser = UserIdentityDataFactory.existingSeededUser();
        HomePage homePage = loginPage.logInAccount(userDataExistingUser);
        cartPage = homePage.getNavBar().navigateToCart();
        List <CartItem> itemsAfterLogin = cartPage.readCartItems();
        Set <String> cartProductNamesAfterLogin = itemsAfterLogin.stream()
                .map(CartItem::getProductName)
                .map(normalizer::normalizeText)
                .collect(Collectors.toSet());
        Assert.assertEquals(cartProductNamesAfterLogin,searchResults);
    }

    @Test(groups = {"regression", "products", "destructive", "slow"})
    public void userCanAddReviewToProduct() {
        ProductsPage productsPage = openProductsPage();
        ProductDetailsPage productDetailsPage = productsPage.viewSingleProduct(SLEEVELESS_DRESS);
        productDetailsPage.addReviewToProduct("Reviewer", "test@review.com","Good product quality");
        Assert.assertEquals(productDetailsPage.getReviewSubmissionConfirmation(),REVIEW_SUCCESS_MESSAGE);
    }

    @Test(groups = {"regression", "products", "cart", "fast"})
    public void userCanAddRecommendedProductToCart() {
        HomePage homePage = openHomePage();
        homePage.addRecommendedProductToCart(SLEEVELESS_DRESS);
        CartPage cartPage = homePage.modal.goToCart();
        Assert.assertEquals(normalizer.normalizeText(cartPage.getCartItemName()), normalizer.normalizeText(SLEEVELESS_DRESS));
    }

}
