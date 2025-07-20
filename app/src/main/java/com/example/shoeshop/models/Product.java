package com.example.shoeshop.models;

import com.google.gson.annotations.SerializedName; // Import này nếu bạn dùng SerializedName
import java.time.Instant; // Import Instant

public class Product {
    private String productID;
    private String productName;
    private String description;
    private String imageUrl;
    // private String imageFile; // imageFile không phải là trường của model trả về từ API, nó chỉ dùng cho request
    private String size;
    private String color;
    private double price;
    private double discount;
    private double total;
    private int soldQuantity;
    private int stockQuantity;
    private boolean status;
    private boolean isActive;

    @SerializedName("createdAt") // Đảm bảo tên này khớp với tên thuộc tính ở backend (thường là lowercase)
    private Instant createdAt; // Thêm trường CreatedAt với kiểu Instant

    // Getters
    public String getProductID() { return productID; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    // public String getImageFile() { return imageFile; } // Bỏ getter này nếu không có trường imageFile
    public String getSize() { return size; }
    public String getColor() { return color; }
    public double getPrice() { return price; }
    public double getDiscount() { return discount; }
    public double getTotal() { return total; }
    public int getSoldQuantity() { return soldQuantity; }
    public int getStockQuantity() { return stockQuantity; }
    public boolean isStatus() { return status; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; } // Thêm getter cho CreatedAt

    // Setters (tùy chọn, Gson có thể tự set qua reflection, nhưng nên có để rõ ràng)
    public void setProductID(String productID) { this.productID = productID; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    // public void setImageFile(String imageFile) { this.imageFile = imageFile; } // Bỏ setter này
    public void setSize(String size) { this.size = size; }
    public void setColor(String color) { this.color = color; }
    public void setPrice(double price) { this.price = price; }
    public void setDiscount(double discount) { this.discount = discount; }
    public void setTotal(double total) { this.total = total; }
    public void setSoldQuantity(int soldQuantity) { this.soldQuantity = soldQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setStatus(boolean status) { this.status = status; }
    public void setActive(boolean active) { isActive = active; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; } // Thêm setter cho CreatedAt
}
