package com.example.shoeshop.models;

public class ChatMessageRequest {
    private String senderID;
    private String message;
    private String chatSessionID;

    public ChatMessageRequest(String senderID, String message, String chatSessionID) {
        this.senderID = senderID;
        this.message = message;
        this.chatSessionID = chatSessionID;
    }

    // Getters and Setters (hoặc chỉ cần constructor nếu bạn không cần set riêng lẻ sau khi tạo)
    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChatSessionID() {
        return chatSessionID;
    }

    public void setChatSessionID(String chatSessionID) {
        this.chatSessionID = chatSessionID;
    }
}
