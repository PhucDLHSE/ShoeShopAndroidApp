package com.example.shoeshop.models;

public class Product {
    private String productID;
    private String productName;
    private String description;
    private String imageUrl;
    private String imageFile;
    private String size;
    private String color;
    private double price;
    private double discount;
    private double total;
    private int soldQuantity;
    private int stockQuantity;
    private boolean status;
    private boolean isActive;

    public String getProductID() { return productID; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getImageFile() { return imageFile; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public double getPrice() { return price; }
    public double getDiscount() { return discount; }
    public double getTotal() { return total; }
    public int getSoldQuantity() { return soldQuantity; }
    public int getStockQuantity() { return stockQuantity; }
    public boolean isStatus() { return status; }
    public boolean isActive() { return isActive; }
}
