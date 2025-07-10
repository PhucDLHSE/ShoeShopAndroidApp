package com.example.shoeshop.models;

import java.util.List;

public class Order {
    private String orderID;
    private String userID;
    private List<OrderDetail> orderDetails;
    private String orderDate;
    private String status;
    private int totalAmount;
    private String deliveryAddress;
    private String methodName;
    private boolean isActive;

    public String getOrderID() { return orderID; }
    public String getUserID() { return userID; }
    public List<OrderDetail> getOrderDetails() { return orderDetails; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public int getTotalAmount() { return totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getMethodName() { return methodName; }
    public boolean isActive() { return isActive; }
    public void setOrderID(String orderID) { this.orderID = orderID; }
    public void setUserID(String userID) { this.userID = userID; }
    public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public void setActive(boolean active) { isActive = active; }
    public static class OrderDetail{
        private String orderDetailID;
        private String orderID;
        private String productID;
        private String productName;
        private String imageUrl;
        private int size;
        private String color;
        private int quantity;
        private long price;
        private long total;
        private String paymentDate;
        private boolean isActive;
        public String getOrderDetailID() { return orderDetailID; }
        public String getOrderID() { return orderID; }
        public String getProductID() { return productID; }
        public String getProductName() { return productName; }
        public String getImageUrl() { return imageUrl; }
        public int getSize() { return size; }
        public String getColor() { return color; }
        public int getQuantity() { return quantity; }
        public long getPrice() { return price; }
        public long getTotal() { return total; }
        public String getPaymentDate() { return paymentDate; }
        public boolean isActive() { return isActive; }
        public void setOrderDetailID(String orderDetailID) { this.orderDetailID = orderDetailID; }
        public void setOrderID(String orderID) { this.orderID = orderID; }
        public void setProductID(String productID) { this.productID = productID; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public void setSize(int size) { this.size = size; }
        public void setColor(String color) { this.color = color; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setPrice(long price) { this.price = price; }
        public void setTotal(long total) { this.total = total; }
        public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
        public void setActive(boolean active) { isActive = active; }

    }
}
