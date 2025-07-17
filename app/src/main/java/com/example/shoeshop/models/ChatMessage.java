package com.example.shoeshop.models;

import java.util.Date; // Hoặc kiểu dữ liệu tương ứng với DateTime.UtcNow từ backend

public class ChatMessage {
    private String chatMessageID; // Optional, nếu bạn cần ID của tin nhắn
    private String chatSessionID;
    private String senderID;
    private String message; // Tương ứng với 'content' trong UI
    private Date sentAt; // Thời gian gửi tin nhắn

    // Constructor cho UI (role, content)
    public ChatMessage(String role, String content) {
        // Ánh xạ role và content cho UI
        this.senderID = role; // Có thể dùng role làm senderID tạm thời cho UI
        this.message = content;
    }

    // Constructor cho dữ liệu từ Backend
    public ChatMessage(String chatMessageID, String chatSessionID, String senderID, String message, Date sentAt) {
        this.chatMessageID = chatMessageID;
        this.chatSessionID = chatSessionID;
        this.senderID = senderID;
        this.message = message;
        this.sentAt = sentAt;
    }

    // Getters và Setters
    public String getChatMessageID() {
        return chatMessageID;
    }

    public void setChatMessageID(String chatMessageID) {
        this.chatMessageID = chatMessageID;
    }

    public String getChatSessionID() {
        return chatSessionID;
    }

    public void setChatSessionID(String chatSessionID) {
        this.chatSessionID = chatSessionID;
    }

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

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    // Helper method để lấy "role" cho UI từ senderID
    public String getRole() {

        return "user".equalsIgnoreCase(senderID) ? "user" : "assistant";
    }

    public void setContent(String content) {
        this.message = content;
    }

    public String getContent() {
        return message;
    }
}
