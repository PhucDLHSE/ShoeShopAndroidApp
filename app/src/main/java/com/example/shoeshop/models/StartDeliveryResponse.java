package com.example.shoeshop.models;

public class StartDeliveryResponse {
    private String message;
    private Data data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String deliveryID;
        private String orderID;
        private String deliveryStatus;
        private String deliveryAddress;
        private String shipperName;
        private String shipperPhone;
        private String deliveryDate;
        private String storageName;
        private String note;
        private boolean isActive;

        public String getDeliveryID() {
            return deliveryID;
        }

        public void setDeliveryID(String deliveryID) {
            this.deliveryID = deliveryID;
        }

        public String getOrderID() {
            return orderID;
        }

        public void setOrderID(String orderID) {
            this.orderID = orderID;
        }

        public String getDeliveryStatus() {
            return deliveryStatus;
        }

        public void setDeliveryStatus(String deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
        }

        public String getDeliveryAddress() {
            return deliveryAddress;
        }

        public void setDeliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
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

        public String getDeliveryDate() {
            return deliveryDate;
        }

        public void setDeliveryDate(String deliveryDate) {
            this.deliveryDate = deliveryDate;
        }

        public String getStorageName() {
            return storageName;
        }

        public void setStorageName(String storageName) {
            this.storageName = storageName;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }
    }
}