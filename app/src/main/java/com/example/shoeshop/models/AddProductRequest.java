package com.example.shoeshop.models;

public class AddProductRequest {
    private String ProductName;
    private String Description;
    private String ImageFile;
    private String ImageUrl;
    private String Size;
    private String Color;
    private Double Price;
    private Double Discount;
    private int SoldQuantity;
    private int StockQuantity;

    public AddProductRequest(String productName, String description, String imageFile, String imageUrl,
                             String size, String color, Double price, Double discount,
                             int soldQuantity, int stockQuantity) {
        ProductName = productName;
        Description = description;
        ImageFile = imageFile;
        ImageUrl = imageUrl;
        Size = size;
        Color = color;
        Price = price;
        Discount = discount;
        SoldQuantity = soldQuantity;
        StockQuantity = stockQuantity;
    }

    //Getter and Setter methods
    public String getProductName() {
        return ProductName;
    }
    public void setProductName(String productName) {
        ProductName = productName;
    }
    public String getDescription() {
        return Description;
    }
    public void setDescription(String description) {
        Description = description;
    }
    public String getImageFile() {
        return ImageFile;
    }
    public void setImageFile(String imageFile) {
        ImageFile = imageFile;
    }
    public String getImageUrl() {
        return ImageUrl;
    }
    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }
    public String getSize() {
        return Size;
    }
    public void setSize(String size) {
        Size = size;
    }
    public String getColor() {
        return Color;
    }
    public void setColor(String color) {
        Color = color;
    }
    public Double getPrice() {
        return Price;
    }
    public void setPrice(Double price) {
        Price = price;
    }
    public Double getDiscount() {
        return Discount;
    }
    public void setDiscount(Double discount) {
        Discount = discount;
    }
    public int getSoldQuantity() {
        return SoldQuantity;
    }
    public void setSoldQuantity(int soldQuantity) {
        SoldQuantity = soldQuantity;
    }
    public int getStockQuantity() {
        return StockQuantity;
    }
    public void setStockQuantity(int stockQuantity) {
        StockQuantity = stockQuantity;
    }
}
