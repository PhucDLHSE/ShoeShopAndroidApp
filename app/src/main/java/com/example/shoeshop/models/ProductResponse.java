package com.example.shoeshop.models;

import java.util.List;
import com.google.gson.annotations.SerializedName; // Import for @SerializedName if needed

public class ProductResponse {
    @SerializedName("count") // Đảm bảo tên trường khớp với JSON
    private int count;

    @SerializedName("products") // Đảm bảo tên trường khớp với JSON
    private List<Product> products;

    // Getter cho count
    public int getCount() {
        return count;
    }

    // Setter cho count (có thể không cần nếu chỉ đọc)
    public void setCount(int count) {
        this.count = count;
    }

    // Getter cho products
    public List<Product> getProducts() {
        return products;
    }

    // Setter cho products (có thể không cần nếu chỉ đọc)
    public void setProducts(List<Product> products) {
        this.products = products;
    }
}