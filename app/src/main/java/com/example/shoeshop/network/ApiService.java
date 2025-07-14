    package com.example.shoeshop.network;

    import com.example.shoeshop.models.AddProductRequest;
    import com.example.shoeshop.models.AddProductResponse;
    import com.example.shoeshop.models.CreateDeliveryRequest;
    import com.example.shoeshop.models.Feedback;
    import com.example.shoeshop.models.PatchDeliveryResponse;
    import com.example.shoeshop.models.CustomerAddress;
    import com.example.shoeshop.models.DeliveryStatusResponse;
    import com.example.shoeshop.models.LoginRequest;
    import com.example.shoeshop.models.LoginResponse;
    import com.example.shoeshop.models.Order;
    import com.example.shoeshop.models.OrderRequest;
    import com.example.shoeshop.models.OrderResponse;
    import com.example.shoeshop.models.Product;
    import com.example.shoeshop.models.PutProductRequest;
    import com.example.shoeshop.models.RegisterRequest;
    import com.example.shoeshop.models.StartOrderResponse;

    import java.util.List;

    import okhttp3.MultipartBody;
    import okhttp3.RequestBody;
    import retrofit2.Call;
    import retrofit2.http.Body;
    import retrofit2.http.DELETE;
    import retrofit2.http.GET;
    import retrofit2.http.Header;
    import retrofit2.http.Multipart;
    import retrofit2.http.PATCH;
    import retrofit2.http.POST;
    import retrofit2.http.PUT;
    import retrofit2.http.Part;
    import retrofit2.http.Path;
    import retrofit2.http.Query;

    public interface ApiService {

        // Authentication endpoints
        @POST("auth/login")
        Call<LoginResponse> login(@Body LoginRequest request);
        @POST("auth/register")
        Call<LoginResponse> register(@Body RegisterRequest request);

        // Product endpoints
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

        //Product endpoints for staff
        @Multipart
        @POST("Product") // Add a new product
        Call<AddProductResponse> addProduct(
                @Header("Authorization") String token,
                @Part("ProductName") RequestBody name,
                @Part("Description") RequestBody desc,
                @Part MultipartBody.Part imageFile,        // file ảnh
                @Part("ImageUrl") RequestBody imageUrl,    // nếu bạn vẫn muốn gửi url
                @Part("Size") RequestBody size,
                @Part("Color") RequestBody color,
                @Part("Price") RequestBody price,
                @Part("Discount") RequestBody discount,
                @Part("SoldQuantity") RequestBody soldQty,
                @Part("StockQuantity") RequestBody stockQty
        );
        @Multipart
        @PUT("Product/{productId}") // Update an existing product
        Call<AddProductResponse> updateProduct(
                @Header("Authorization") String token,
                @Path("productId") String productId,
                @Part("ProductName") RequestBody name,
                @Part("Description") RequestBody desc,
                @Part MultipartBody.Part imageFile,
                @Part("ImageUrl") RequestBody imageUrl,
                @Part("Size") RequestBody size,
                @Part("Color") RequestBody color,
                @Part("Price") RequestBody price,
                @Part("Discount") RequestBody discount,
                @Part("StockQuantity") RequestBody stockQty
        );

        @DELETE("Product/{productId}/hard") // Delete a product
        Call<Void> deleteProduct(
                @Header("Authorization") String token,
                @Path("productId") String productId
        );

        // Customer address endpoints
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

        // Order endpoints
        @POST("Order/create-order")
        Call<OrderResponse> createOrder(
                @Header("Authorization") String token,
                @Body OrderRequest request
        );
        @GET("Order/status/ordered")
        Call<List<Order>> getOrderedOrders(@Header("Authorization") String token);
        @GET("Order/status/processing")
        Call<List<Order>> getProcessingOrders(@Header("Authorization") String token);
        @GET("Order/status/waiting-ship")
        Call<List<Order>> getWaitingShipOrders(@Header("Authorization") String token);
        @GET("Order/status/shipping")
        Call<List<Order>> getShippingOrders(@Header("Authorization") String token);
        @GET("Order/status/completed")
        Call<List<Order>> getCompleteOrders(@Header("Authorization") String token);
        @GET("Order/status/cancled")
        Call<List<Order>> getCanceledOrders(@Header("Authorization") String token);
        @PUT("Order/{orderId}/start-processing") // Bắt đầu xử lý đơn hàng
         Call<StartOrderResponse> startProcessingOrder(
                @Header("Authorization") String token,
                @Path("orderId") String orderId
        );

        @PUT("Order/{orderId}/pending-ship") // Đặt đơn hàng chờ vận chuyển
        Call<StartOrderResponse> pendingShipOrder(
                @Header("Authorization") String token,
                @Path("orderId") String orderId
        );

        @PUT("Order/{orderId}/cancel") // Hủy đơn hàng
        Call<StartOrderResponse> cancelOrder(
                @Header("Authorization") String token,
                @Path("orderId") String orderId
        );

        //Delivery endpoints
        //Tạo phiếu giao hàng
        @POST("Delivery")
        Call<PatchDeliveryResponse> createDelivery(
                @Header("Authorization") String token,
                @Body CreateDeliveryRequest request
        );
        @GET("Delivery/status/{status}") // Lấy danh sách phiếu giao hàng theo trạng thái, input status: chờ lấy hàng, đang giao hàng, đã giao hàng
        Call<List<DeliveryStatusResponse>> getDeliveriesByStatus(
                @Header("Authorization") String token,
                @Path("status") String status
        );
        @PATCH("Delivery/{deliveryId}/start") // Bắt đầu giao hàng
        Call<PatchDeliveryResponse> startDelivery(
                @Header("Authorization") String token,
                @Path("deliveryId") String deliveryId
        );

        @PATCH("Delivery/{deliveryId}/complete") // Hoàn thành giao hàng
        Call<PatchDeliveryResponse> completeDelivery(
                @Header("Authorization") String token,
                @Path("deliveryId") String deliveryId
        );
        @PATCH("Delivery/{deliveryId}/cancel") // Hủy giao hàng
        Call<PatchDeliveryResponse> cancelDelivery(
                @Header("Authorization") String token,
                @Path("deliveryId") String deliveryId
        );

        //Feedback endpoints
        @GET("Feedback/all") // Lấy tất cả phản hồi
        Call<List<Feedback>> getAllFeedbacks();
        @GET("Feedback/user/{userId}") // Lấy phản hồi của người dùng
        Call<List<Feedback>> getUserFeedbacks(
                @Header("Authorization") String token,
                @Path("userId") String userId
        );
        @GET("Feedback/product/{productId}") // Lấy phản hồi của sản phẩm
        Call<List<Feedback>> getProductFeedbacks(
                @Header("Authorization") String token,
                @Path("productId") String productId
        );
        @DELETE("Feedback/{feedbackId}/hard") // Xóa phản hồi
        Call<Void> deleteFeedback(
                @Header("Authorization") String token,
                @Path("feedbackId") String feedbackId
        );

    }
