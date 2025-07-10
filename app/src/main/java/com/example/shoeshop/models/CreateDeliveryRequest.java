package com.example.shoeshop.models;

public class CreateDeliveryRequest {
    private String orderID;
    private String shipperName;
    private String shipperPhone;
    private String note;

    public CreateDeliveryRequest(String orderID, String shipperName, String shipperPhone, String note) {
        this.orderID = orderID;
        this.shipperName = shipperName;
        this.shipperPhone = shipperPhone;
        this.note = note;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getShipperName() {
        return shipperName;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }

    public String getShipperPhone() {
        return shipperPhone;
    }

    public void setShipperPhone(String shipperPhone) {
        this.shipperPhone = shipperPhone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}