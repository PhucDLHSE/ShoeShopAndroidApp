package com.example.shoeshop.models;

import java.time.Instant; // Thay đổi từ java.util.Date sang java.time.Instant

public class ChatMessage {
    private String chatMessageID; // Optional, nếu bạn cần ID của tin nhắn
    private String chatSessionID;
    private String senderID;
    private String message; // Tương ứng với 'content' trong UI
    private Instant sentAt; // Thời gian gửi tin nhắn - Đã đổi sang Instant

    // Constructor cho UI (role, content)
    public ChatMessage(String role, String content) {
        this.senderID = role;
        this.message = content;
        this.sentAt = Instant.now(); // Khởi tạo Instant khi tạo tin nhắn UI
    }

    // Constructor cho dữ liệu từ Backend
    public ChatMessage(String chatMessageID, String chatSessionID, String senderID, String message, Instant sentAt) {
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

    public Instant getSentAt() { // Đã đổi kiểu trả về
        return sentAt;
    }

    public void setSentAt(Instant sentAt) { // Đã đổi kiểu tham số
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