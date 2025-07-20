package com.example.shoeshop.models;

import java.util.List;
import com.google.gson.annotations.SerializedName; // Import này là cần thiết

public class ProductResponse {
    @SerializedName("count") // Đảm bảo tên trường khớp với JSON
    private int count;

    @SerializedName("products") // Đảm bảo tên trường khớp với JSON
    private List<Product> products;

    @SerializedName("latestProductTimestamp") // Thêm trường này để nhận timestamp từ backend
    private String latestProductTimestamp; // Kiểu String để khớp với định dạng ISO 8601

    // Constructor (tùy chọn, nhưng hữu ích nếu bạn khởi tạo đối tượng thủ công)
    public ProductResponse(int count, List<Product> products, String latestProductTimestamp) {
        this.count = count;
        this.products = products;
        this.latestProductTimestamp = latestProductTimestamp;
    }

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

    // Getter cho latestProductTimestamp
    public String getLatestProductTimestamp() {
        return latestProductTimestamp;
    }

    // Setter cho latestProductTimestamp (có thể không cần nếu chỉ đọc)
    public void setLatestProductTimestamp(String latestProductTimestamp) {
        this.latestProductTimestamp = latestProductTimestamp;
    }
}
