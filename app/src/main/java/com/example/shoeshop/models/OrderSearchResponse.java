package com.example.shoeshop.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderSearchResponse {
    @SerializedName("orders")
    private List<Order> orders;

    @SerializedName("orderCount")
    private int orderCount;

    @SerializedName("totalSum")
    private double totalSum;

    // Getters and setters
    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public double getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(double totalSum) {
        this.totalSum = totalSum;
    }
}
