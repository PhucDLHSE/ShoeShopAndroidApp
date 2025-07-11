package com.example.shoeshop.models;

public class CartItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private String imageUrl;

    // NEW: trạng thái được chọn trong giỏ hàng
    private boolean isSelected = false;

    public CartItem(String productId,
                    String productName,
                    String imageUrl,
                    double price,
                    int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }

    // --- Getter / Setter hiện tại ---
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // --- NEW: Getter / Setter cho isSelected ---
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }
}
