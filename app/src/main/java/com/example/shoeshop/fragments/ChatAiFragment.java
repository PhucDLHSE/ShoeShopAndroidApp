package com.example.shoeshop.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeshop.R;
import com.example.shoeshop.adapters.ChatAdapter;
import com.example.shoeshop.models.ChatMessage;
import com.example.shoeshop.models.Product;
import com.example.shoeshop.network.ApiClient;
import com.example.shoeshop.network.ApiService;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnSend.setBackgroundTintList(null);
        recyclerChat = view.findViewById(R.id.recyclerChat);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerChat.setAdapter(chatAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnSend.setOnClickListener(v -> {
            String userInput = etMessage.getText().toString().trim();
            if (!userInput.isEmpty()) {
                btnSend.setEnabled(false);
                addMessage("user", userInput);
                addMessage("assistant", "🤖 AI đang tìm kiếm sản phẩm...");
                etMessage.setText("");
                handleSearch(userInput);
            }
        });
    }

    private void addMessage(String role, String content) {
        messageList.add(new ChatMessage(role, content));
        requireActivity().runOnUiThread(() -> {
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerChat.scrollToPosition(messageList.size() - 1);
        });
    }

    private void replaceLastAssistantMessage(String newContent) {
        requireActivity().runOnUiThread(() -> {
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

    private void handleSearch(String prompt) {
        ProductSearchInfo info = extractProductInfo(prompt.toLowerCase(Locale.getDefault()));
        Log.d("ChatAI", "Search: name=" + info.productName + ", size=" + info.size + ", color=" + info.color
                + ", minPrice=" + info.minPrice + ", maxPrice=" + info.maxPrice);

        Call<List<Product>> call = apiService.searchProducts(
                info.productName, info.size, info.color, info.minPrice, info.maxPrice
        );

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    if (products.isEmpty()) {
                        replaceLastAssistantMessage("Không tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn.");
                    } else {
                        StringBuilder reply = new StringBuilder("Tìm thấy các sản phẩm sau:\n");
                        for (Product product : products) {
                            reply.append("- ").append(product.getProductName())
                                    .append(" (Size: ").append(product.getSize())
                                    .append(", Màu: ").append(product.getColor())
                                    .append(", Giá: ").append(String.format(Locale.getDefault(), "%,.0f", product.getTotal()))
                                    .append(" VNĐ)\n");
                        }
                        replaceLastAssistantMessage(reply.toString().trim());
                    }
                } else {
                    replaceLastAssistantMessage("❌ Lỗi từ máy chủ: " + response.code());
                }
                handler.postDelayed(() -> btnSend.setEnabled(true), 1000);
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                replaceLastAssistantMessage("❌ Lỗi kết nối: " + t.getMessage());
                handler.post(() -> btnSend.setEnabled(true));
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
                    !prompt.contains("size") && !prompt.contains("màu") && !prompt.contains("giá")) {
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
            if (priceString.contains("k")) return Double.parseDouble(priceString.replace("k", "")) * 1000;
            if (priceString.contains("tr") || priceString.contains("triệu")) return Double.parseDouble(priceString.replace("tr", "").replace("triệu", "")) * 1_000_000;
            return Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Log.e("ChatAI", "Error parsing price: '" + priceString + "'", e);
            return null;
        }
    }
}
