package com.example.shoeshop.models;

import java.util.List;

public class OrderRequest {
    private String fullAddress;
    private List<ProductOrderDetail> productOrderDetails;

    public OrderRequest(String fullAddress, List<ProductOrderDetail> productOrderDetails) {
        this.fullAddress = fullAddress;
        this.productOrderDetails = productOrderDetails;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public List<ProductOrderDetail> getProductOrderDetails() {
        return productOrderDetails;
    }

    public void setProductOrderDetails(List<ProductOrderDetail> productOrderDetails) {
        this.productOrderDetails = productOrderDetails;
    }
}