package com.example.shoeshop.models;
public class Order {
    private String orderID;
    private String userID;
    private String orderDate;
    private String status;
    private int totalAmount;
    private String deliveryAddress;
    private String methodName;
    private boolean isActive;

    public String getOrderID() { return orderID; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public int getTotalAmount() { return totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getMethodName() { return methodName; }
}
