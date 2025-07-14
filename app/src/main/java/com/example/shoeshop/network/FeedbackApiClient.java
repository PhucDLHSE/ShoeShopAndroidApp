// app/src/main/java/com/example/shoeshop/network/FeedbackApiClient.java
package com.example.shoeshop.network;

import com.example.shoeshop.utils.CustomDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Date;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FeedbackApiClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "https://shoesale-f9gsd3anbyd8ctbu.southeastasia-01.azurewebsites.net/api/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new CustomDateAdapter())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(FeedbackApiClient.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}