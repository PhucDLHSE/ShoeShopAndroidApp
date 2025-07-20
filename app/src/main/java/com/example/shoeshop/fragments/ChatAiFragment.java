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
import com.example.shoeshop.Interface.NewProductListener;
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
    private ApiService apiService;
    private SessionManager sessionManager;
    private NewProductListener newProductListener; // Đây là MainActivity
    private String currentChatSessionId;
    private String currentUserId;
    private Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private final long POLLING_INTERVAL = 30 * 1000; // 30 giây

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NewProductListener) {
            newProductListener = (NewProductListener) context;
            sessionManager = new SessionManager(context);
            // KHÔNG ĐẶT setListener ở đây cho ChatAiFragment.
            // MainActivity là đối tượng lắng nghe chính của SessionManager để nhận thông báo sản phẩm mới.
            // ChatAiFragment sẽ *đọc* dữ liệu sản phẩm mới trực tiếp từ SessionManager.
            // (Đã bỏ: sessionManager.setNewProductListener(newProductListener);)
        } else {
            throw new RuntimeException(context.toString() + " must implement NewProductListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        newProductListener = null;
    }

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
        btnSend.setBackgroundTintList(null); // Đảm bảo màu nền nút không bị ảnh hưởng
        recyclerChat = view.findViewById(R.id.recyclerChat);
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(chatAdapter);
        apiService = ApiClient.getClient().create(ApiService.class);

        if (sessionManager == null && getContext() != null) {
            sessionManager = new SessionManager(getContext());
        }

        currentUserId = sessionManager.getUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy User ID. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        // Ban đầu xóa tin nhắn trước khi tải, để đảm bảo trạng thái sạch
        messageList.clear();
        chatAdapter.notifyDataSetChanged();

        // Bắt đầu hoặc tải phiên chat
        startOrLoadChatSession(currentUserId);

        btnSend.setOnClickListener(v -> {
            String userInput = etMessage.getText().toString().trim();
            if (!userInput.isEmpty()) {
                if (currentChatSessionId == null || currentChatSessionId.isEmpty()) {
                    Toast.makeText(getContext(), "Phiên chat chưa sẵn sàng, đang khởi tạo lại...", Toast.LENGTH_SHORT).show();
                    startOrLoadChatSession(currentUserId);
                    return;
                }
                btnSend.setEnabled(false);
                addMessage("user", userInput);
                saveChatMessage(currentUserId, userInput, currentChatSessionId);

                addMessage("assistant", "🤖 AI đang tìm kiếm sản phẩm...");
                etMessage.setText("");
                handleSearch(userInput);
            }
        });

        // Polling Runnable: CHỈ KÍCH HOẠT KIỂM TRA SẢN PHẨM MỚI TỪ SESSIONMANAGER.
        // KHÔNG GỌI displayNewProductsIfAny() TRỰC TIẾP TỪ POLLING NÀY.
        // Vì displayNewProductsIfAny() sẽ được gọi khi fragment hiển thị và nhận được dữ liệu.
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                sessionManager.checkNewProductsFromApi(); // SessionManager sẽ thông báo cho MainActivity
                pollingHandler.postDelayed(this, POLLING_INTERVAL);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        startPolling();
        // Khi fragment resume, ta CHỈ CẦN kiểm tra và hiển thị sản phẩm mới nếu có
        // mà SessionManager đã tải về. KHÔNG cần gọi SessionManager.checkNewProductsFromApi() ở đây.
        // Việc này đã do MainActivity thực hiện thông qua listener của nó.
        displayNewProductsIfAny();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPolling();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPolling();
    }

    private void startOrLoadChatSession(String userId) {
        final String savedChatSessionId = sessionManager.getChatSessionIdForUser(userId);

        if (savedChatSessionId != null && !savedChatSessionId.isEmpty()) {
            currentChatSessionId = savedChatSessionId;
            Log.d("ChatAI", "Loaded existing chat session: " + currentChatSessionId + " for user: " + userId);

            addMessage("assistant", "Đang tải lịch sử chat...");

            apiService.getChatMessages(currentChatSessionId).enqueue(new Callback<List<ChatMessage>>() {
                @Override
                public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            // Xóa tin nhắn "đang tải" hoặc lỗi trước đó
                            removeLoadingOrErrorMessage();

                            messageList.clear(); // Xóa sạch để thêm lịch sử mới

                            if (response.isSuccessful() && response.body() != null) {
                                List<ChatMessage> fetchedMessages = response.body();
                                if (fetchedMessages.isEmpty()) {
                                    Log.d("ChatAI", "No chat messages found for session: " + currentChatSessionId);
                                    addMessage("assistant", "Chào mừng bạn trở lại! Không có tin nhắn nào trong phiên này. Hãy bắt đầu một cuộc trò chuyện mới.");
                                } else {
                                    Log.d("ChatAI", "Loaded " + fetchedMessages.size() + " messages for session: " + currentChatSessionId);
                                    for (com.example.shoeshop.models.ChatMessage msg : fetchedMessages) {
                                        addMessage(msg.getSenderID().equals(currentUserId) ? "user" : "assistant", msg.getMessage());
                                    }
                                }
                            } else {
                                Log.e("ChatAI", "Failed to load chat messages: " + response.code() + " - " + response.message());
                                addMessage("assistant", "Lỗi khi tải lịch sử chat: " + response.code() + " - " + response.message());
                            }

                            // SAU KHI TẤT CẢ LỊCH SỬ ĐÃ ĐƯỢC THÊM VÀO, BÂY GIỜ HIỂN THỊ SẢN PHẨM MỚI.
                            // displayNewProductsIfAny() sẽ kiểm tra và hiển thị nếu có.
                            displayNewProductsIfAny();
                            recyclerChat.scrollToPosition(messageList.size() - 1);
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                    Log.e("ChatAI", "Error loading chat messages", t);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            removeLoadingOrErrorMessage();
                            addMessage("assistant", "Lỗi kết nối khi tải lịch sử chat: " + t.getMessage());
                            displayNewProductsIfAny(); // Vẫn kiểm tra SP mới ngay cả khi lỗi tải lịch sử
                            recyclerChat.scrollToPosition(messageList.size() - 1);
                        });
                    }
                }
            });

        } else {
            Log.d("ChatAI", "No existing chat session found for user: " + userId + ". Starting a new one.");
            startNewChatSession(userId);
        }
    }

    private void startNewChatSession(String userId) {
        messageList.clear(); // Luôn xóa khi bắt đầu phiên mới
        chatAdapter.notifyDataSetChanged();
        addMessage("assistant", "Đang khởi tạo phiên chat mới...");

        apiService.startChatSession(userId).enqueue(new Callback<ChatSessionResponse>() {
            @Override
            public void onResponse(Call<ChatSessionResponse> call, Response<ChatSessionResponse> response) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        removeLoadingOrErrorMessage();

                        if (response.isSuccessful() && response.body() != null) {
                            currentChatSessionId = response.body().getChatSessionID();
                            sessionManager.saveChatSessionIdForUser(userId, currentChatSessionId);
                            Log.d("ChatAI", "Started new chat session: " + currentChatSessionId + " for user: " + userId);
                            addMessage("assistant", "Chào bạn, tôi là trợ lý AI. Bạn muốn tìm sản phẩm nào?");
                            displayNewProductsIfAny(); // Sau khi khởi tạo, kiểm tra và hiển thị SP mới
                        } else {
                            Log.e("ChatAI", "Failed to start chat session: " + response.code() + " - " + response.message());
                            Toast.makeText(getContext(), "Không thể bắt đầu phiên chat.", Toast.LENGTH_SHORT).show();
                            currentChatSessionId = null;
                            addMessage("assistant", "Lỗi: Không thể bắt đầu phiên chat. Mã lỗi: " + response.code());
                        }
                        recyclerChat.scrollToPosition(messageList.size() - 1);
                    });
                }
            }

            @Override
            public void onFailure(Call<ChatSessionResponse> call, Throwable t) {
                Log.e("ChatAI", "Error starting chat session", t);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        removeLoadingOrErrorMessage();
                        Toast.makeText(getContext(), "Lỗi kết nối khi bắt đầu chat.", Toast.LENGTH_SHORT).show();
                        currentChatSessionId = null;
                        addMessage("assistant", "Lỗi kết nối khi bắt đầu chat: " + t.getMessage());
                        recyclerChat.scrollToPosition(messageList.size() - 1);
                    });
                }
            }
        });
    }

    // Helper method to remove loading/error messages
    private void removeLoadingOrErrorMessage() {
        if (!messageList.isEmpty() && "assistant".equals(messageList.get(messageList.size() - 1).getRole())) {
            String lastAssistantMsgContent = messageList.get(messageList.size() - 1).getContent();
            if (lastAssistantMsgContent.equals("Đang tải lịch sử chat...") ||
                    lastAssistantMsgContent.equals("Đang khởi tạo phiên chat mới...") ||
                    lastAssistantMsgContent.startsWith("Lỗi khi tải lịch sử chat:") ||
                    lastAssistantMsgContent.startsWith("Lỗi kết nối khi tải lịch sử chat:")) {
                messageList.remove(messageList.size() - 1);
                chatAdapter.notifyItemRemoved(messageList.size());
            }
        }
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
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerChat.scrollToPosition(messageList.size() - 1);
            });
        }
    }

    private void replaceLastAssistantMessage(String newContent) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                for (int i = messageList.size() - 1; i >= 0; i--) {
                    if ("assistant".equals(messageList.get(i).getRole())) {
                        messageList.get(i).setContent(newContent);
                        chatAdapter.notifyItemChanged(i);
                        recyclerChat.scrollToPosition(i);
                        return;
                    }
                }
                addMessage("assistant", newContent); // Fallback if no assistant message found
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
                                    .append(" Giá: ").append(String.format(Locale.getDefault(), "%,.0f", product.getTotal()))
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
                        btnSend.setEnabled(true);
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

        // Tối ưu hóa việc trích xuất tên sản phẩm
        Matcher nameMatcherWithKeyword = Pattern.compile("(?:giày|dép|sản phẩm)\\s+([\\p{L}\\d\\s]{2,30})", Pattern.CASE_INSENSITIVE).matcher(prompt);
        if (nameMatcherWithKeyword.find()) {
            info.productName = nameMatcherWithKeyword.group(1).trim();
        } else {
            // Logic cũ vẫn có thể được giữ lại làm fallback hoặc tinh chỉnh
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
            // Kiểm tra các màu phổ biến
            for (String color : new String[]{"đỏ", "đen", "trắng", "xanh", "xanh dương", "xanh lá", "vàng", "nâu", "cam", "hồng", "đasắc", "bạc"}) {
                if (prompt.contains(color)) {
                    info.color = color;
                    break;
                }
            }
            // Xử lý trường hợp đặc biệt "trắng đen"
            if (info.color == null && prompt.contains("trắng đen")) {
                info.color = "trắng đen";
            }
        }

        // Tinh chỉnh regex cho giá để nó linh hoạt hơn
        Matcher priceRange = Pattern.compile("giá\\s*(?:từ)?\\s*(\\d+(?:k|tr|triệu)?)\\s*(?:đến|-|tới|dưới)?\\s*(\\d+(?:k|tr|triệu)?)?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(prompt);
        if (priceRange.find()) {
            info.minPrice = parsePrice(priceRange.group(1));
            // Nếu có group 2 và nó không rỗng, parse maxPrice
            if (priceRange.group(2) != null && !priceRange.group(2).isEmpty()) {
                info.maxPrice = parsePrice(priceRange.group(2));
            } else {
                // Nếu chỉ có một số tiền và không có "đến", có thể coi đó là giá chính xác hoặc giá tối thiểu
                info.maxPrice = info.minPrice; // Hoặc để null nếu bạn muốn tìm kiếm giá chính xác
            }
        } else {
            // Xử lý các trường hợp giá "trên X", "dưới Y", "giá X"
            info.minPrice = parsePrice(extractParameter(prompt, "(?:từ|trên|hơn|giá từ)\\s*(\\d+(?:k|tr|triệu)?)", 1));
            info.maxPrice = parsePrice(extractParameter(prompt, "(?:đến|dưới|giá đến|giá khoảng)\\s*(\\d+(?:k|tr|triệu)?)", 1));
            // Thêm trường hợp chỉ có một giá duy nhất mà không có từ khóa "từ", "đến"
            if (info.minPrice == null && info.maxPrice == null) {
                Double exactPrice = parsePrice(extractParameter(prompt, "(?:giá là|giá)\\s*(\\d+(?:k|tr|triệu)?)", 1));
                if (exactPrice != null) {
                    info.minPrice = exactPrice;
                    info.maxPrice = exactPrice;
                }
            }
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

    private void startPolling() {
        if (pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
            pollingHandler.post(pollingRunnable);
            Log.d("ChatAI", "Started new product polling (from ChatAiFragment).");
        }
    }

    private void stopPolling() {
        if (pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
            Log.d("ChatAI", "Stopped new product polling (from ChatAiFragment).");
        }
    }

    /**
     * Hiển thị danh sách sản phẩm mới trong chat nếu có.
     * Phương thức này được gọi khi người dùng vào ChatAiFragment và phiên chat được tải/khởi tạo.
     * Nó cũng sẽ dọn dẹp dữ liệu sản phẩm mới sau khi hiển thị.
     */
    private void displayNewProductsIfAny() {
        Log.d("ChatAiFragment", "displayNewProductsIfAny() called.");
        List<Product> newProducts = sessionManager.getNewProducts();

        // Chỉ hiển thị nếu CÓ sản phẩm mới VÀ cờ "hasNewProducts" đang TRUE
        // Cờ "hasNewProducts" được đặt bởi `checkNewProductsFromApi` trong SessionManager (qua MainActivity)
        if (sessionManager.hasNewProducts() && newProducts != null && !newProducts.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder("🎉 Hôm nay có ");
            messageBuilder.append(newProducts.size()).append(" sản phẩm mới vừa ra mắt! Hãy xem ngay:\n");

            for (Product product : newProducts) {
                messageBuilder.append("- ").append(product.getProductName())
                        .append(" (Giá: ").append(String.format(Locale.getDefault(), "%,.0f", product.getTotal()))
                        .append(" VNĐ)\n");
            }
            addMessage("assistant", messageBuilder.toString().trim());
            saveChatMessage("assistant", messageBuilder.toString().trim(), currentChatSessionId);

            // SAU KHI ĐÃ CHẮC CHẮN HIỂN THỊ, XÓA DỮ LIỆU SẢN PHẨM MỚI VÀ ĐẶT CỜ HAS_NEW_PRODUCTS VỀ FALSE
            sessionManager.clearNewProducts();
            sessionManager.setHasNewProducts(false); // Đặt lại cờ có sản phẩm mới

            Log.d("ChatAiFragment", "New products displayed and cleared from SessionManager.");

            // Thông báo cho MainActivity rằng sản phẩm mới đã được hiển thị trong chat
            if (newProductListener != null) {
                newProductListener.onNewProductsDisplayedInChat(); // Gọi để MainActivity ẩn badge
                Log.d("ChatAiFragment", "Notified MainActivity that new products were displayed in chat.");
            }
        } else {
            Log.d("ChatAiFragment", "No new products to display or already displayed/cleared.");
        }
    }
}