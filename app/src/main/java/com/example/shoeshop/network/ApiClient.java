package com.example.shoeshop.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.Instant; // Import Instant
import com.example.shoeshop.adapters.InstantAdapter; // Import InstantAdapter

public class ApiClient {
    private static final String BASE_URL = "https://shoesale-f9gsd3anbyd8ctbu.southeastasia-01.azurewebsites.net/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Tạo một logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Tạo một OkHttpClient và thêm interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            // --- Cấu hình Gson để xử lý java.time.Instant ---
            GsonBuilder gsonBuilder = new GsonBuilder();
            // Đăng ký InstantAdapter để Gson biết cách tuần tự hóa/giải tuần tự hóa Instant
            gsonBuilder.registerTypeAdapter(Instant.class, new InstantAdapter());

            // Không cần setDateFormat trực tiếp nữa vì InstantAdapter sẽ xử lý định dạng ISO 8601
            // .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS") // Dòng này không còn cần thiết

            Gson gson = gsonBuilder.create();

            // Xây dựng Retrofit với OkHttpClient và GsonConverterFactory tùy chỉnh
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
