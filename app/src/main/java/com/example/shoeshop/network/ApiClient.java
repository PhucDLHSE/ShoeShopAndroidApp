package com.example.shoeshop.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://shoesale-f9gsd3anbyd8ctbu.southeastasia-01.azurewebsites.net/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Tạo một logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            // Đặt mức độ log mong muốn
            // BODY: Log headers, body, và metadata
            // HEADERS: Log headers và metadata
            // BASIC: Log phương thức request, URL, mã phản hồi, và thông báo phản hồi
            // NONE: Không log
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Tạo một OkHttpClient và thêm interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // --- BẮT ĐẦU PHẦN CẬP NHẬT ĐỂ XỬ LÝ LỖI PHÂN TÍCH NGÀY THÁNG ---
            // Tạo một Gson instance tùy chỉnh
            Gson gson = new GsonBuilder()
                    // Đặt định dạng ngày tháng để khớp với định dạng từ backend.
                    // 'SSSSSSS' biểu thị 7 chữ số thập phân cho giây (nanoseconds/microseconds).
                    // Đây là định dạng mà lỗi của bạn đã chỉ ra: '2025-07-17T17:17:57.1350987'
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
                    .create();

            // Xây dựng Retrofit với OkHttpClient và GsonConverterFactory tùy chỉnh
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    // Sử dụng GsonConverterFactory với Gson instance tùy chỉnh
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}