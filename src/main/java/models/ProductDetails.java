package models;

public class ProductDetails {
    private final String name;
    private final String category;
    private final String price;
    private final String availability;
    private final String condition;
    private final String brand;

    public ProductDetails(String name, String category, String price,
                          String availability, String condition, String brand) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.availability = availability;
        this.condition = condition;
        this.brand = brand;
    }

    // getters
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getPrice() { return price; }
    public String getAvailability() { return availability; }
    public String getCondition() { return condition; }
    public String getBrand() { return brand; }
}

