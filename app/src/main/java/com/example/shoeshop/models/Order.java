package com.example.shoeshop.models;

import com.google.gson.annotations.SerializedName;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Order {

    @SerializedName("orderID")
    private String orderID;

    @SerializedName("userID")
    private String userID;

    @SerializedName("orderDetails")
    private List<OrderDetail> orderDetails;

    @SerializedName("orderDate")
    private String orderDate;

    @SerializedName("status")
    private String status;

    @SerializedName("totalAmount")
    private long totalAmount;

    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("methodName")
    private String methodName;

    @SerializedName("isActive")
    private boolean isActive;

    /* ------------ GETTER / SETTER ------------- */
    public String getOrderID()                   { return orderID; }
    public void   setOrderID(String orderID)     { this.orderID = orderID; }

    public String getUserID()                    { return userID; }
    public void   setUserID(String userID)       { this.userID = userID; }

    public List<OrderDetail> getOrderDetails()   { return orderDetails; }
    public void   setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public String getOrderDate()                 { return orderDate; }
    public void   setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getStatus()                    { return status; }
    public void   setStatus(String status)       { this.status = status; }

    public long   getTotalAmount()               { return totalAmount; }
    public void   setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDeliveryAddress()           { return deliveryAddress; }
    public void   setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getMethodName()                { return methodName; }
    public void   setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isActive()                    { return isActive; }
    public void   setActive(boolean active)      { isActive = active; }

    public String getFirstProductName() {
        return (orderDetails != null && !orderDetails.isEmpty())
                ? orderDetails.get(0).getProductName()
                : "";
    }

    public String getFirstImageUrl() {
        return (orderDetails != null && !orderDetails.isEmpty())
                ? orderDetails.get(0).getImageUrl()
                : "";
    }

    public String getTotalFormatted() {
        return NumberFormat
                .getInstance(new Locale("vi", "VN"))
                .format(totalAmount) + "đ";
    }

    public static class OrderDetail {

        @SerializedName("orderDetailID")
        private String orderDetailID;

        @SerializedName("orderID")
        private String orderID;

        @SerializedName("productID")
        private String productID;

        @SerializedName("productName")
        private String productName;

        @SerializedName("imageUrl")
        private String imageUrl;

        @SerializedName("size")
        private String size;
        @SerializedName("color")
        private String color;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("price")
        private long price;

        @SerializedName("total")
        private long total;

        @SerializedName("paymentDate")
        private String paymentDate;

        @SerializedName("isActive")
        private boolean isActive;

        @SerializedName("isReviewed")
        private boolean isReviewed;

        public boolean isReviewed() {
            return isReviewed;
        }

        /* ---- Getter / Setter ---- */
        public String getOrderDetailID()                { return orderDetailID; }
        public void   setOrderDetailID(String id)       { this.orderDetailID = id; }

        public String getOrderID()                      { return orderID; }
        public void   setOrderID(String orderID)        { this.orderID = orderID; }

        public String getProductID()                    { return productID; }
        public void   setProductID(String productID)    { this.productID = productID; }

        public String getProductName()                  { return productName; }
        public void   setProductName(String name)       { this.productName = name; }

        public String getImageUrl()                     { return imageUrl; }
        public void   setImageUrl(String imageUrl)      { this.imageUrl = imageUrl; }

        public String getSize()                         { return size; }
        public void   setSize(String size)              { this.size = size; }

        public String getColor()                        { return color; }
        public void   setColor(String color)            { this.color = color; }

        public int getQuantity()                        { return quantity; }
        public void setQuantity(int quantity)           { this.quantity = quantity; }

        public long getPrice()                          { return price; }
        public void setPrice(long price)                { this.price = price; }

        public long getTotal()                          { return total; }
        public void setTotal(long total)                { this.total = total; }

        public String getPaymentDate()                  { return paymentDate; }
        public void setPaymentDate(String paymentDate)  { this.paymentDate = paymentDate; }

        public boolean isActive()                       { return isActive; }
        public void setActive(boolean active)           { isActive = active; }
    }
}
