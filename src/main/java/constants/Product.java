package constants;

import java.math.BigDecimal;

public enum Product {
    BLUE_TOP("Blue Top", new BigDecimal("500")),
    MEN_TSHIRT("Men Tshirt", new BigDecimal("400"), "Men > Tshirts", "In Stock", "New", "H&M"),
    SLEEVELESS_DRESS("Sleeveless Dress", new BigDecimal("1000")),
    STYLISH_DRESS("Stylish Dress", new BigDecimal("1500")),
    WINTER_TOP("Winter Top", new BigDecimal("600")),
    SUMMER_WHITE_TOP("Summer White Top", new BigDecimal("400")),
    MADAME_TOP_FOR_WOMEN("Madame Top For Women", new BigDecimal("1000")),
    FANCY_GREEN_TOP("Fancy Green Top", new BigDecimal("700")),
    SLEEVES_PRINTED_TOP_WHITE("Sleeves Printed Top - White", new BigDecimal("499")),
    HALF_SLEEVES_TOP_SCHIFFLI_DETAILING_PINK("Half Sleeves Top Schiffli Detailing - Pink", new BigDecimal("359")),
    FROZEN_TOPS_FOR_KIDS("Frozen Tops For Kids", new BigDecimal("278")),
    FULL_SLEEVES_TOP_CHERRY_PINK("Full Sleeves Top Cherry - Pink", new BigDecimal("679")),
    PRINTED_OFF_SHOULDER_TOP_WHITE("Printed Off Shoulder Top - White", new BigDecimal("315")),
    SLEEVES_TOP_AND_SHORT_BLUE_PINK("Sleeves Top and Short - Blue & Pink", new BigDecimal("478")),
    LITTLE_GIRLS_MR_PANDA_SHIRT("Little Girls Mr. Panda Shirt", new BigDecimal("1200")),
    SLEEVELESS_UNICORN_PATCH_GOWN_PINK("Sleeveless Unicorn Patch Gown - Pink", new BigDecimal("1050")),
    COTTON_MULL_EMBROIDERED_DRESS("Cotton Mull Embroidered Dress", new BigDecimal("1190")),
    BLUE_COTTON_INDIE_MICKEY_DRESS("Blue Cotton Indie Mickey Dress", new BigDecimal("1530")),
    LONG_MAXI_TULLE_FANCY_DRESS_UP_OUTFITS_PINK("Long Maxi Tulle Fancy Dress Up Outfits -Pink", new BigDecimal("1600")),
    SLEEVELESS_UNICORN_PRINT_FIT_FLARE_NET_DRESS_MULTI(
            "Sleeveless Unicorn Print Fit & Flare Net Dress - Multi", new BigDecimal("1100")),
    COLOUR_BLOCKED_SHIRT_SKY_BLUE("Colour Blocked Shirt – Sky Blue", new BigDecimal("849")),
    PURE_COTTON_V_NECK_T_SHIRT("Pure Cotton V-Neck T-Shirt", new BigDecimal("1299")),
    GREEN_SIDE_PLACKET_DETAIL_T_SHIRT("Green Side Placket Detail T-Shirt", new BigDecimal("1000")),
    PREMIUM_POLO_T_SHIRTS("Premium Polo T-Shirts", new BigDecimal("1500")),
    PURE_COTTON_NEON_GREEN_TSHIRT("Pure Cotton Neon Green Tshirt", new BigDecimal("850")),
    SOFT_STRETCH_JEANS("Soft Stretch Jeans", new BigDecimal("799")),
    REGULAR_FIT_STRAIGHT_JEANS("Regular Fit Straight Jeans", new BigDecimal("1200")),
    GRUNT_BLUE_SLIM_FIT_JEANS("Grunt Blue Slim Fit Jeans", new BigDecimal("1400")),
    ROSE_PINK_EMBROIDERED_MAXI_DRESS("Rose Pink Embroidered Maxi Dress", new BigDecimal("2300")),
    COTTON_SILK_HAND_BLOCK_PRINT_SAREE("Cotton Silk Hand Block Print Saree", new BigDecimal("3000")),
    RUST_RED_LINEN_SAREE("Rust Red Linen Saree", new BigDecimal("3500")),
    BEAUTIFUL_PEACOCK_BLUE_COTTON_LINEN_SAREE("Beautiful Peacock Blue Cotton Linen Saree", new BigDecimal("5000")),
    LACE_TOP_FOR_WOMEN("Lace Top For Women", new BigDecimal("1400")),
    GRAPHIC_DESIGN_MEN_T_SHIRT_BLUE("GRAPHIC DESIGN MEN T SHIRT - BLUE", new BigDecimal("1389"));

    private final String productName;
    private final BigDecimal productPrice;
    private final String categories;
    private final String availability;
    private final String condition;
    private final String brandName;

    Product(
            String productName,
            BigDecimal productPrice,
            String categories,
            String availability,
            String condition,
            String brandName) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.categories = categories;
        this.availability = availability;
        this.condition = condition;
        this.brandName = brandName;
    }

    Product(String productName, BigDecimal productPrice) {
        this(productName, productPrice, null, null, null, null);
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public String getCategories() {
        return categories;
    }

    public String getAvailability() {
        return availability;
    }

    public String getCondition() {
        return condition;
    }

    public String getBrandName() {
        return brandName;
    }
}
