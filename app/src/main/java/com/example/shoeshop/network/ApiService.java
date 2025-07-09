    package com.example.shoeshop.network;

    import com.example.shoeshop.models.CustomerAddress;
    import com.example.shoeshop.models.LoginRequest;
    import com.example.shoeshop.models.LoginResponse;
    import com.example.shoeshop.models.Order;
    import com.example.shoeshop.models.OrderRequest;
    import com.example.shoeshop.models.OrderResponse;
    import com.example.shoeshop.models.Product;
    import com.example.shoeshop.models.RegisterRequest;

    import java.util.List;

    import okhttp3.ResponseBody;
    import retrofit2.Call;
    import retrofit2.http.Body;
    import retrofit2.http.GET;
    import retrofit2.http.Header;
    import retrofit2.http.PATCH;
    import retrofit2.http.POST;
    import retrofit2.http.Path;
    import retrofit2.http.Query;

    public interface ApiService {
        @POST("auth/login")
        Call<LoginResponse> login(@Body LoginRequest request);
        @POST("auth/register")
        Call<LoginResponse> register(@Body RegisterRequest request);

        @GET("Product")
        Call<List<Product>> getAllProducts();
        @GET("Product/{productId}")
        Call<Product> getProductById(@Path("productId") String productId);


        @GET("Product/search")
        Call<List<Product>> searchProducts(
                @Query("ProductName") String productName,
                @Query("Size") String size,
                @Query("Color") String color,
                @Query("MinPrice") Double minPrice,
                @Query("MaxPrice") Double maxPrice
        );

        @GET("CustomerAddresses/my-addresses")
        Call<List<CustomerAddress>> getMyAddresses(@Header("Authorization") String token);

        @POST("CustomerAddresses")
        Call<CustomerAddress> addCustomerAddress(
                @Header("Authorization") String token,
                @Body CustomerAddress address
        );
        @PATCH("CustomerAddresses/set-default/{addressId}")
        Call<Void> setDefaultAddress(
                @Header("Authorization") String token,
                @Path("addressId") String addressId
        );

        @POST("Order/create-order")
        Call<OrderResponse> createOrder(
                @Header("Authorization") String token,
                @Body OrderRequest request
        );
        @GET("Order/status/ordered")
        Call<List<Order>> getOrderedOrders(@Header("Authorization") String token);
        @GET("api/Order/status/processing")
        Call<List<Order>> getProcessingOrders(@Header("Authorization") String token);
        @GET("Order/status/waiting-ship")
        Call<List<Order>> getWaitingShipOrders(@Header("Authorization") String token);
        @GET("Order/status/shipping")
        Call<List<Order>> getShippingOrders(@Header("Authorization") String token);
        @GET("Order/status/complete")
        Call<List<Order>> getCompleteOrders(@Header("Authorization") String token);

    }
