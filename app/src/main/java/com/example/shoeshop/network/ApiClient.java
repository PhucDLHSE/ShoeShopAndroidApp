package com.example.shoeshop.network;

import android.os.Build;

import com.example.shoeshop.adapters.InstantAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://shoesale-f9gsd3anbyd8ctbu.southeastasia-01.azurewebsites.net/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();
            Gson gson = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")  // hỗ trợ các field Date nếu có
                        .registerTypeAdapter(Instant.class, new InstantAdapter())
                        .create();
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}