package models;

import java.math.BigDecimal;

public class CartItem {
    private final String productName;
    private final BigDecimal price;
    private final int quantity;
    private final BigDecimal totalPrice;

    public CartItem(String productName, BigDecimal price, int quantity, BigDecimal totalPrice) {
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }
    public String getProductName() {
        return productName;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public Integer getQuantity() {return quantity;}
    public BigDecimal getTotalPrice() {return totalPrice;}
}

