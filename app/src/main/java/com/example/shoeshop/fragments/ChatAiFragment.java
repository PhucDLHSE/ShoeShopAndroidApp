package com.example.shoeshop.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.example.shoeshop.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatAiFragment extends Fragment {

    private EditText etMessage;
    private ImageButton btnSend;
    private RecyclerView recyclerChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ApiService apiService;
    private SessionManager sessionManager;

    private String currentChatSessionId;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnSend.setBackgroundTintList(null); // Đảm bảo tint list không ảnh hưởng
        recyclerChat = view.findViewById(R.id.recyclerChat);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(chatAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(getContext());

        currentUserId = sessionManager.getUserId();

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy User ID. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            // Optional: Chuyển hướng về LoginActivity nếu User ID không có
            // Intent intent = new Intent(getContext(), LoginActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(intent);
            return;
        }

        // Luôn clear danh sách tin nhắn và adapter khi View của Fragment được tạo lại.
        // Điều này đảm bảo giao diện luôn mới trước khi tải hoặc tạo session.
        messageList.clear();
        chatAdapter.notifyDataSetChanged();

        // Bắt đầu hoặc tải phiên chat dựa trên UserID và Session đã lưu.
        startOrLoadChatSession(currentUserId);

        btnSend.setOnClickListener(v -> {
            String userInput = etMessage.getText().toString().trim();
            if (!userInput.isEmpty()) {
                // Kiểm tra lại currentChatSessionId trước khi gửi tin nhắn
                if (currentChatSessionId == null || currentChatSessionId.isEmpty()) {
                    Toast.makeText(getContext(), "Phiên chat chưa sẵn sàng, đang khởi tạo lại...", Toast.LENGTH_SHORT).show();
                    // Thử khởi tạo lại session nếu nó bị null (ví dụ: lỗi mạng trước đó)
                    startOrLoadChatSession(currentUserId);
                    return; // Ngăn gửi tin nhắn nếu session ID chưa có
                }

                btnSend.setEnabled(false); // Vô hiệu hóa nút gửi để tránh spam
                addMessage("user", userInput); // Hiển thị tin nhắn người dùng ngay lập tức
                saveChatMessage(currentUserId, userInput, currentChatSessionId); // Lưu tin nhắn người dùng

                addMessage("assistant", "🤖 AI đang tìm kiếm sản phẩm..."); // Hiển thị tin nhắn chờ của AI
                etMessage.setText(""); // Xóa nội dung EditText
                handleSearch(userInput); // Gửi yêu cầu tìm kiếm sản phẩm
            }
        });
    }

    /**
     * Quyết định tải phiên chat hiện có hoặc bắt đầu một phiên chat mới cho người dùng.
     * Phương thức này được gọi mỗi khi Fragment được tạo hoặc tái tạo View.
     * @param userId ID của người dùng hiện tại.
     */
    private void startOrLoadChatSession(String userId) {
        // Cố gắng lấy session ID đã lưu cho userId hiện tại
        final String savedChatSessionId = sessionManager.getChatSessionIdForUser(userId);

        if (savedChatSessionId != null && !savedChatSessionId.isEmpty()) {
            // Trường hợp 1: Có session ID đã lưu cho user này (chưa logout)
            currentChatSessionId = savedChatSessionId;
            Log.d("ChatAI", "Loaded existing chat session: " + currentChatSessionId + " for user: " + userId);

            // Xóa bất kỳ tin nhắn chờ nào và thêm tin nhắn "Đang tải..." mới
            messageList.clear(); // Xóa sạch để chuẩn bị hiển thị lịch sử
            chatAdapter.notifyDataSetChanged();
            addMessage("assistant", "Đang tải lịch sử chat...");

            // Tải lịch sử tin nhắn của phiên hiện tại
            loadChatMessages(currentChatSessionId);

        } else {
            // Trường hợp 2: Không có session ID đã lưu cho user này
            // (có thể là lần đầu chat, hoặc đã logout và login lại)
            Log.d("ChatAI", "No existing chat session found for user: " + userId + ". Starting a new one.");
            startNewChatSession(userId);
        }
    }

    /**
     * Bắt đầu một phiên chat mới trên Backend và lưu ID phiên.
     * @param userId ID của người dùng hiện tại.
     */
    private void startNewChatSession(String userId) {
        // Đảm bảo UI trống rỗng cho một session mới
        messageList.clear();
        chatAdapter.notifyDataSetChanged();
        addMessage("assistant", "Đang khởi tạo phiên chat mới..."); // Thông báo cho người dùng

        apiService.startChatSession(userId).enqueue(new Callback<ChatSessionResponse>() {
            @Override
            public void onResponse(Call<ChatSessionResponse> call, Response<ChatSessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentChatSessionId = response.body().getChatSessionID();
                    // Lưu ChatSessionID vào SessionManager với userId cụ thể
                    sessionManager.saveChatSessionIdForUser(userId, currentChatSessionId);
                    Log.d("ChatAI", "Started new chat session: " + currentChatSessionId + " for user: " + userId);
                    // Sau khi tạo thành công session mới, hiển thị tin nhắn chào mừng chính thức
                    replaceLastAssistantMessage("Chào bạn, tôi là trợ lý AI. Bạn muốn tìm sản phẩm nào?");
                } else {
                    Log.e("ChatAI", "Failed to start chat session: " + response.code() + " - " + response.message());
                    Toast.makeText(getContext(), "Không thể bắt đầu phiên chat.", Toast.LENGTH_SHORT).show();
                    currentChatSessionId = null; // Đảm bảo null nếu có lỗi
                    replaceLastAssistantMessage("Lỗi: Không thể bắt đầu phiên chat. Mã lỗi: " + response.code()); // Cập nhật tin nhắn lỗi chi tiết hơn
                }
            }

            @Override
            public void onFailure(Call<ChatSessionResponse> call, Throwable t) {
                Log.e("ChatAI", "Error starting chat session", t);
                Toast.makeText(getContext(), "Lỗi kết nối khi bắt đầu chat.", Toast.LENGTH_SHORT).show();
                currentChatSessionId = null; // Đảm bảo null nếu có lỗi
                replaceLastAssistantMessage("Lỗi kết nối khi bắt đầu chat: " + t.getMessage()); // Cập nhật tin nhắn lỗi chi tiết hơn
            }
        });
    }

    /**
     * Tải lịch sử tin nhắn của một phiên chat cụ thể từ Backend.
     * @param sessionId ID của phiên chat cần tải lịch sử.
     */
    private void loadChatMessages(String sessionId) {
        apiService.getChatMessages(sessionId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatMessage> fetchedMessages = response.body();

                    if (isAdded()) { // Đảm bảo Fragment vẫn còn được gắn vào Activity
                        requireActivity().runOnUiThread(() -> {
                            // Xóa tin nhắn "Đang tải lịch sử chat..." ban đầu
                            if (!messageList.isEmpty() && "assistant".equals(messageList.get(messageList.size() - 1).getRole())) {
                                String lastAssistantMsgContent = messageList.get(messageList.size() - 1).getContent();
                                if (lastAssistantMsgContent.equals("Đang tải lịch sử chat...") ||
                                        lastAssistantMsgContent.startsWith("Lỗi khi tải lịch sử chat:") ||
                                        lastAssistantMsgContent.startsWith("Lỗi kết nối khi tải lịch sử chat:")) {
                                    messageList.remove(messageList.size() - 1);
                                    chatAdapter.notifyItemRemoved(messageList.size());
                                }
                            }

                            // Xóa toàn bộ danh sách hiện có và thêm các tin nhắn đã fetch
                            messageList.clear(); // Clear lại lần nữa để chắc chắn trước khi add data

                            if (fetchedMessages.isEmpty()) {
                                Log.d("ChatAI", "No chat messages found for session: " + sessionId);
                                addMessage("assistant", "Chào mừng bạn trở lại! Không có tin nhắn nào trong phiên này. Hãy bắt đầu một cuộc trò chuyện mới.");
                            } else {
                                Log.d("ChatAI", "Loaded " + fetchedMessages.size() + " messages for session: " + sessionId);
                                for (com.example.shoeshop.models.ChatMessage msg : fetchedMessages) {
                                    addMessage(msg.getSenderID().equals(currentUserId) ? "user" : "assistant", msg.getMessage());
                                }
                                // Thêm một tin nhắn chào mừng hoặc kết thúc sau khi tải lịch sử hoàn tất
                                addMessage("assistant", "Chào mừng bạn trở lại! Đây là lịch sử chat của bạn.");
                            }
                            // Cuộn xuống cuối sau khi tất cả tin nhắn đã được thêm
                            recyclerChat.scrollToPosition(messageList.size() - 1);
                        });
                    }

                } else {
                    Log.e("ChatAI", "Failed to load chat messages: " + response.code() + " - " + response.message());
                    if (isAdded()) {
                        replaceLastAssistantMessage("Lỗi khi tải lịch sử chat: " + response.code() + " - " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Log.e("ChatAI", "Error loading chat messages", t);
                if (isAdded()) {
                    replaceLastAssistantMessage("Lỗi kết nối khi tải lịch sử chat: " + t.getMessage());
                }
            }
        });
    }

    private void saveChatMessage(String senderId, String messageContent, String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            Log.e("ChatAI", "Cannot save message: ChatSessionID is null or empty.");
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

    private void addMessage(String role, String content) {
        messageList.add(new ChatMessage(role, content));
        if (isAdded()) { // Kiểm tra Fragment có được gắn vào Activity không
            requireActivity().runOnUiThread(() -> {
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerChat.scrollToPosition(messageList.size() - 1);
            });
        }
    }

    private void replaceLastAssistantMessage(String newContent) {
        if (isAdded()) { // Kiểm tra Fragment có được gắn vào Activity không
            requireActivity().runOnUiThread(() -> {
                // Duyệt ngược từ cuối để tìm tin nhắn của assistant
                for (int i = messageList.size() - 1; i >= 0; i--) {
                    if ("assistant".equals(messageList.get(i).getRole())) {
                        messageList.get(i).setContent(newContent);
                        chatAdapter.notifyItemChanged(i);
                        recyclerChat.scrollToPosition(i);
                        return; // Đã tìm thấy và cập nhật, thoát vòng lặp
                    }
                }
                // Nếu không tìm thấy tin nhắn assistant nào để thay thế (ví dụ: danh sách rỗng), thêm tin nhắn mới
                addMessage("assistant", newContent);
            });
        }
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

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        replaceLastAssistantMessage(aiReply);
                        saveChatMessage("assistant", aiReply, currentChatSessionId);
                        btnSend.setEnabled(true); // Kích hoạt lại nút gửi
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                String aiReply = "❌ Lỗi kết nối: " + t.getMessage();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        replaceLastAssistantMessage(aiReply);
                        saveChatMessage("assistant", aiReply, currentChatSessionId);
                        btnSend.setEnabled(true);
                    });
                }
            }
        });
    }

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