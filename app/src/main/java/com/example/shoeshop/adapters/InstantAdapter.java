package com.example.shoeshop.adapters;

import android.util.Log; // Đảm bảo import Log

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneOffset; // Import ZoneOffset
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField; // Import ChronoField

/**
 * Custom Gson TypeAdapter for serializing and deserializing java.time.Instant objects.
 * This adapter is designed to be more robust in parsing ISO 8601 strings,
 * especially those with varying fractional second precision and missing 'Z' (assuming UTC).
 */
public class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

    // Formatter linh hoạt để phân tích chuỗi ngày giờ địa phương hoặc không có offset
    // Nó có thể xử lý các phần giây với 0 đến 9 chữ số thập phân.
    private static final DateTimeFormatter FLEXIBLE_DATE_TIME_PARSER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            // Cho phép phần thập phân của giây từ 0 đến 9 chữ số, và dấu chấm là tùy chọn
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .toFormatter();

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        // Instant.toString() tự nhiên tạo ra chuỗi ISO 8601 với độ chính xác nanosecond
        // nếu có, hoặc millisecond. Điều này ổn cho việc tuần tự hóa.
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateString = json.getAsString();
        try {
            // Thử phân tích cú pháp trực tiếp bằng Instant.parse() (mong đợi 'Z' hoặc offset)
            return Instant.parse(dateString);
        } catch (DateTimeParseException e) {
            // Nếu Instant.parse() thất bại, có thể do thiếu 'Z' hoặc định dạng phần giây không chuẩn.
            // Chúng ta sẽ thử phân tích nó như một LocalDateTime và sau đó chuyển đổi sang Instant,
            // giả định nó là thời gian UTC (vì backend thường lưu trữ UTC).
            try {
                // Phân tích chuỗi thành LocalDateTime bằng formatter linh hoạt của chúng ta
                java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(dateString, FLEXIBLE_DATE_TIME_PARSER);
                // Chuyển đổi LocalDateTime sang Instant bằng cách giả định nó là UTC.
                // Đây là một cách tiếp cận phổ biến và mạnh mẽ khi backend gửi thời gian không có offset.
                return localDateTime.toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e2) {
                // Nếu vẫn thất bại sau nhiều lần thử, ghi log lỗi chi tiết
                Log.e("InstantAdapter", "Failed to parse Instant after multiple attempts for: '" + dateString + "'. Error: " + e2.getMessage());
                throw new JsonParseException("Failed to parse Instant: " + dateString, e2);
            }
        }
    }
}

