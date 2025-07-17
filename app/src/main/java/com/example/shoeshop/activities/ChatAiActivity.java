package com.example.shoeshop.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.ChatAdapter;
import com.example.shoeshop.models.ChatMessage;
import com.example.shoeshop.models.ChatMessageRequest;
import com.example.shoeshop.models.ChatSessionResponse;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;
import com.example.shoeshop.utils.SessionManager; // IMPORT SESSSIONMANAGER

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatAiActivity extends AppCompatActivity {

    private EditText etMessage;
    private Button btnSend;
    private RecyclerView recyclerChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ApiService apiService;
    private SessionManager sessionManager; // KHAI BÁO SESSIONMANAGER

    private String currentChatSessionId;
    private String currentUserId; // Lấy UserID của người dùng đang đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSend.setBackgroundTintList(null);
        recyclerChat = findViewById(R.id.recyclerChat);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this); // KHỞI TẠO SESSIONMANAGER

        // Lấy UserID từ SessionManager
        currentUserId = sessionManager.getUserId(); // LẤY USERID TỪ SESSIONMANAGER

        if (currentUserId == null || currentUserId.isEmpty()) { // KIỂM TRA CẢ NULL VÀ RỖNG
            Toast.makeText(this, "Không tìm thấy User ID. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            // Có thể chuyển hướng về màn hình đăng nhập
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            finish();
            return;
        }

        // Bắt đầu hoặc tải phiên chat
        startOrLoadChatSession(currentUserId);

        btnSend.setOnClickListener(view -> {
            String userInput = etMessage.getText().toString().trim();
            if (!userInput.isEmpty()) {
                btnSend.setEnabled(false);
                addMessage("user", userInput); // Thêm tin nhắn người dùng vào UI
                // Lưu tin nhắn người dùng vào backend
                saveChatMessage(currentUserId, userInput, currentChatSessionId);

                addMessage("assistant", "🤖 AI đang tìm kiếm sản phẩm..."); // Hiển thị trạng thái AI
                etMessage.setText("");
                handleSearch(userInput);
            }
        });
    }

    private void startOrLoadChatSession(String userId) {
        // Kiểm tra xem có ChatSessionId nào đang hoạt động trong SharedPreferences không
        // Giữ nguyên đoạn này vì nó liên quan đến session chat cụ thể, không phải session user
        SharedPreferences sharedPref = getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE);
        String savedSessionId = sharedPref.getString("currentChatSessionId", null);

        if (savedSessionId != null) {
            currentChatSessionId = savedSessionId;
            Log.d("ChatAI", "Loaded existing chat session: " + currentChatSessionId);
            // Tải lịch sử tin nhắn của phiên hiện tại
            loadChatMessages(currentChatSessionId);
        } else {
            // Nếu không có, bắt đầu một phiên chat mới
            startNewChatSession(userId);
        }
    }

    private void startNewChatSession(String userId) {
        apiService.startChatSession(userId).enqueue(new Callback<ChatSessionResponse>() {
            @Override
            public void onResponse(Call<ChatSessionResponse> call, Response<ChatSessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentChatSessionId = response.body().getChatSessionID();
                    // Lưu ChatSessionID vào SharedPreferences
                    SharedPreferences sharedPref = getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("currentChatSessionId", currentChatSessionId);
                    editor.apply();
                    Log.d("ChatAI", "Started new chat session: " + currentChatSessionId);
                    addMessage("assistant", "Chào bạn, tôi là trợ lý AI. Bạn muốn tìm sản phẩm nào?");
                } else {
                    Log.e("ChatAI", "Failed to start chat session: " + response.code());
                    Toast.makeText(ChatAiActivity.this, "Không thể bắt đầu phiên chat.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatSessionResponse> call, Throwable t) {
                Log.e("ChatAI", "Error starting chat session", t);
                Toast.makeText(ChatAiActivity.this, "Lỗi kết nối khi bắt đầu chat.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChatMessages(String sessionId) {
        apiService.getChatMessages(sessionId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messageList.clear(); // Xóa tin nhắn giả lập
                    for (com.example.shoeshop.models.ChatMessage msg : response.body()) {
                        // Backend trả về message. role được xác định dựa trên senderID
                        messageList.add(new ChatMessage(msg.getSenderID().equals(currentUserId) ? "user" : "assistant", msg.getMessage()));
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerChat.scrollToPosition(messageList.size() - 1);
                    addMessage("assistant", "Chào mừng bạn trở lại! Bạn muốn tìm sản phẩm nào nữa?");
                } else {
                    Log.e("ChatAI", "Failed to load chat messages: " + response.code());
                    addMessage("assistant", "Lỗi khi tải lịch sử chat.");
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Log.e("ChatAI", "Error loading chat messages", t);
                addMessage("assistant", "Lỗi kết nối khi tải lịch sử chat.");
            }
        });
    }

    private void saveChatMessage(String senderId, String messageContent, String sessionId) {
        if (sessionId == null) {
            Log.e("ChatAI", "Cannot save message: ChatSessionID is null.");
            return;
        }
        ChatMessageRequest messageRequest = new ChatMessageRequest(senderId, messageContent, sessionId);
        apiService.sendChatMessage(messageRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ChatAI", "Message saved successfully.");
                } else {
                    Log.e("ChatAI", "Failed to save message: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ChatAI", "Error saving message", t);
            }
        });
    }

    private void handleSearch(String prompt) {
        ProductSearchInfo info = extractProductInfo(prompt.toLowerCase(Locale.getDefault()));

        Log.d("ChatAI", "Search: name=" + info.productName + ", size=" + info.size + ", color=" + info.color
                + ", minPrice=" + info.minPrice + ", maxPrice=" + info.maxPrice);

        Log.d("ChatAI", "Parsed minPrice: " + info.minPrice + ", Parsed maxPrice: " + info.maxPrice);

        Call<List<Product>> call = apiService.searchProducts(
                info.productName, info.size, info.color, info.minPrice, info.maxPrice
        );

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                String aiReply;
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    if (products.isEmpty()) {
                        aiReply = "Không tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn.";
                    } else {
                        StringBuilder reply = new StringBuilder("Tìm thấy các sản phẩm sau:\n");
                        for (Product product : products) {
                            reply.append("- ").append(product.getProductName())
                                    .append(" (Size: ").append(product.getSize())
                                    .append(", Màu: ").append(product.getColor())
                                    .append(", Giá: ").append(String.format(Locale.getDefault(), "%,.0f", product.getTotal()))
                                    .append(" VNĐ)\n");
                        }
                        aiReply = reply.toString().trim();
                    }
                } else {
                    aiReply = "❌ Lỗi từ máy chủ: " + response.code();
                }
                replaceLastAssistantMessage(aiReply);
                // Sau khi AI trả lời, lưu tin nhắn của AI vào backend
                saveChatMessage("assistant", aiReply, currentChatSessionId); // "assistant" là senderID cho AI
                handler.postDelayed(() -> btnSend.setEnabled(true), 1000);
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                String aiReply = "❌ Lỗi kết nối: " + t.getMessage();
                replaceLastAssistantMessage(aiReply);
                // Sau khi AI trả lời (lỗi), lưu tin nhắn của AI vào backend
                saveChatMessage("assistant", aiReply, currentChatSessionId);
                handler.post(() -> btnSend.setEnabled(true));
            }
        });
    }

    private void addMessage(String role, String content) {
        messageList.add(new ChatMessage(role, content));
        runOnUiThread(() -> {
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerChat.scrollToPosition(messageList.size() - 1);
        });
    }

    private void replaceLastAssistantMessage(String newContent) {
        runOnUiThread(() -> {
            for (int i = messageList.size() - 1; i >= 0; i--) {
                if ("assistant".equals(messageList.get(i).getRole())) {
                    messageList.get(i).setContent(newContent);
                    chatAdapter.notifyItemChanged(i);
                    recyclerChat.scrollToPosition(i);
                    break;
                }
            }
        });
    }

    // ================== NLP Section ===================

    private static class ProductSearchInfo {
        String productName;
        String size;
        String color;
        Double minPrice;
        Double maxPrice;
    }

    private ProductSearchInfo extractProductInfo(String prompt) {
        ProductSearchInfo info = new ProductSearchInfo();

        Matcher nameMatcherWithKeyword = Pattern.compile("(?:giày|dép|sản phẩm)\\s+((?!size|màu|giá)[\\p{L}\\d\\s]{2,30})", Pattern.CASE_INSENSITIVE).matcher(prompt);
        if (nameMatcherWithKeyword.find()) {
            info.productName = nameMatcherWithKeyword.group(1).trim();
        } else {
            if (prompt.length() >= 2 && prompt.length() <= 30 &&
                    !prompt.contains("size") && !prompt.contains("màu") && !prompt.contains("giá") &&
                    !prompt.contains("từ") && !prompt.contains("đến") &&
                    !prompt.contains("trên") && !prompt.contains("dưới") &&
                    !prompt.contains("là") && !prompt.contains("gì")
            ) {
                info.productName = prompt;
            } else if (prompt.contains("giày")) {
                info.productName = "giày";
            } else if (prompt.contains("dép")) {
                info.productName = "dép";
            }
        }

        info.size = extractParameter(prompt, "\\b(?:giày|size|số|cỡ)\\s*(\\d{1,2})\\b", 1);

        info.color = extractParameter(prompt, "(?:màu|màu sắc)\\s+([^,.]+)", 1);
        if (info.color == null) {
            for (String color : new String[]{"đỏ", "đen", "trắng", "xanh", "xanh dương", "xanh lá", "vàng", "nâu", "cam", "hồng", "đasắc", "bạc"}) {
                if (prompt.contains(color)) {
                    info.color = color;
                    break;
                }
            }
        }

        Matcher priceRange = Pattern.compile("giá\\s*từ\\s*(\\d+(?:k|tr|triệu)?)\\s*đến\\s*(\\d+(?:k|tr|triệu)?)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(prompt);
        if (priceRange.find()) {
            info.minPrice = parsePrice(priceRange.group(1));
            info.maxPrice = parsePrice(priceRange.group(2));
        } else {
            info.minPrice = parsePrice(extractParameter(prompt, "(?:từ|trên|hơn|giá từ)\\s*(\\d+(?:k|tr|triệu)?)", 1));
            info.maxPrice = parsePrice(extractParameter(prompt, "(?:đến|dưới|giá đến)\\s*(\\d+(?:k|tr|triệu)?)", 1));
        }

        return info;
    }

    private String extractParameter(String text, String regex, int groupIndex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(groupIndex).trim() : null;
    }

    private Double parsePrice(String priceString) {
        if (priceString == null) return null;
        priceString = priceString.replace(".", "").replace(",", "").toLowerCase();
        try {
            if (priceString.contains("k")) {
                return Double.parseDouble(priceString.replace("k", "")) * 1000;
            }
            if (priceString.contains("tr") || priceString.contains("triệu")) {
                return Double.parseDouble(priceString.replace("tr", "").replace("triệu", "")) * 1_000_000;
            }
            return Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Log.e("ChatAI", "Error parsing price: '" + priceString + "'", e);
            return null;
        }
    }
}