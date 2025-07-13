package com.example.shoeshop.models;

// AddProductResponse.java
//
// This class represents the response structure for adding a product.
// It contains fields corresponding to product details, including
// product identification, descriptive information, pricing,
// and inventory quantities.

public class AddProductResponse {

    // Unique identifier for the product.
    private String productID;

    // Name of the product.
    private String productName;

    // Detailed description of the product.
    private String description;

    // URL to the product image.
    private String imageUrl;

    // File name or identifier for the product image.
    private String imageFile;

    // Size of the product (e.g., "M", "L", "XL").
    private String size;

    // Color of the product.
    private String color;

    // Original price of the product.
    private double price;

    // Discount applied to the product (e.g., 0.10 for 10% off).
    private double discount;

    // Total price after applying discount (price - (price * discount)).
    private double total;

    // Quantity of the product that has been sold.
    private int soldQuantity;

    // Quantity of the product currently in stock.
    private int stockQuantity;

    // Status of the product (e.g., available, out of stock).
    private boolean status;

    // Indicates if the product is active or visible.
    private boolean isActive;

    /**
     * Default constructor for AddProductResponse.
     */
    public AddProductResponse() {
        // Initialize default values if necessary
    }

    /**
     * Parameterized constructor for AddProductResponse.
     *
     * @param productID      Unique identifier for the product.
     * @param productName    Name of the product.
     * @param description    Detailed description of the product.
     * @param imageUrl       URL to the product image.
     * @param imageFile      File name or identifier for the product image.
     * @param size           Size of the product.
     * @param color          Color of the product.
     * @param price          Original price of the product.
     * @param discount       Discount applied to the product.
     * @param total          Total price after applying discount.
     * @param soldQuantity   Quantity of the product that has been sold.
     * @param stockQuantity  Quantity of the product currently in stock.
     * @param status         Status of the product.
     * @param isActive       Indicates if the product is active.
     */
    public AddProductResponse(
            String productID, String productName, String description,
            String imageUrl, String imageFile, String size, String color,
            double price, double discount, double total,
            int soldQuantity, int stockQuantity, boolean status, boolean isActive) {
        this.productID = productID;
        this.productName = productName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.imageFile = imageFile;
        this.size = size;
        this.color = color;
        this.price = price;
        this.discount = discount;
        this.total = total;
        this.soldQuantity = soldQuantity;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.isActive = isActive;
    }

    // --- Getters and Setters for all fields ---

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(int soldQuantity) {
        this.soldQuantity = soldQuantity;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Returns a string representation of the AddProductResponse object.
     *
     * @return A string containing all product details.
     */
    @Override
    public String toString() {
        return "AddProductResponse{" +
                "productID='" + productID + '\'' +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageFile='" + imageFile + '\'' +
                ", size='" + size + '\'' +
                ", color='" + color + '\'' +
                ", price=" + price +
                ", discount=" + discount +
                ", total=" + total +
                ", soldQuantity=" + soldQuantity +
                ", stockQuantity=" + stockQuantity +
                ", status=" + status +
                ", isActive=" + isActive +
                '}';
    }
}
