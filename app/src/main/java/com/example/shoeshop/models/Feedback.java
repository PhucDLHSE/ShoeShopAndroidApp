package com.example.shoeshop.models;

import java.util.Date;

public class Feedback {
        private String feedbackID;
        private String userID;
        private String name;
        private String productID;
        private String productName;
        private int rating;
        private String comment;
        private String createdAt;
        private boolean isActive;

    // Getters and Setters
    public String getFeedbackID() {
        return feedbackID;
    }
    public void setFeedbackID(String feedbackID) {
        this.feedbackID = feedbackID;
    }
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getProductID() {
        return productID;
    }
    public void setProductID(String productID) {
        this.productID = productID;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
