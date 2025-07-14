package com.example.shoeshop.models;

public class FeedbackRequest {
    private String productID;
    private int rating;
    private String comment;

    public FeedbackRequest(String productID, int rating, String comment) {
        this.productID = productID;
        this.rating = rating;
        this.comment = comment;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
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
}
