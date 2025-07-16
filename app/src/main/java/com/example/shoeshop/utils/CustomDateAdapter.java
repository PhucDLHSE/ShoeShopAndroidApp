package com.example.shoeshop.utils;
// app/src/main/java/com/example/shoeshop/utils/CustomDateAdapter.java
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CustomDateAdapter extends TypeAdapter<Date> {
    private final List<SimpleDateFormat> formats;
    private static final SimpleDateFormat UI_OUTPUT_FORMAT =
            new SimpleDateFormat("HH:mm 'ngày' dd/MM/yyyy", new Locale("vi", "VN"));

    public CustomDateAdapter() {
        formats = Arrays.asList(
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'", Locale.US)
        );
        for (SimpleDateFormat f : formats) f.setTimeZone(TimeZone.getTimeZone("UTC"));
        UI_OUTPUT_FORMAT.setTimeZone(TimeZone.getDefault()); // Ví dụ: giờ địa phương
    }

    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        if (value == null) { out.nullValue(); return; }
        out.value(formats.get(0).format(value));
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        String s = in.nextString();
        for (SimpleDateFormat f : formats) {
            try { return f.parse(s); } catch (ParseException ignored) {}
        }
        throw new IOException("Unparseable date: " + s);
    }
    /**
     * Phương thức tĩnh để định dạng một chuỗi ngày tháng từ backend thành định dạng hiển thị cho UI.
     * @param rawBackendDate Chuỗi ngày tháng từ backend (ví dụ: "2025-07-13T05:33:50.5324948")
     * @return Chuỗi ngày tháng đã được định dạng cho UI (ví dụ: "12:33 ngày 13/07/2025" nếu giờ địa phương là UTC+7)
     *         hoặc chuỗi gốc nếu không thể parse.
     */
    public static String formatBackendDateForUI(String rawBackendDate) {
        if (rawBackendDate == null || rawBackendDate.isEmpty()) {
            return "N/A"; // Hoặc thông báo lỗi phù hợp
        }

        // Tạo một instance tạm thời của các định dạng backend để parse
        // (Hoặc bạn có thể làm cho backendFormats là static nếu không thay đổi)
        // Đây là cách đơn giản, nhưng nếu gọi nhiều lần, việc tạo new SimpleDateFormat lặp lại là không tối ưu.
        List<SimpleDateFormat> parsers = Arrays.asList(
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'", Locale.US)
        );
        for (SimpleDateFormat p : parsers) {
            p.setTimeZone(TimeZone.getTimeZone("UTC")); // Đảm bảo parse là UTC
        }

        Date parsedDate = null;
        for (SimpleDateFormat parser : parsers) {
            try {
                parsedDate = parser.parse(rawBackendDate);
                if (parsedDate != null) break; // Thoát nếu parse thành công
            } catch (ParseException ignored) {
                // Tiếp tục thử định dạng khác
            }
        }

        if (parsedDate != null) {
            // Bây giờ định dạng Date object đã parse được sang định dạng UI
            // UI_OUTPUT_FORMAT đã được thiết lập múi giờ (ví dụ: TimeZone.getDefault())
            return UI_OUTPUT_FORMAT.format(parsedDate);
        } else {
            androidx.media3.common.util.Log.w("CustomDateAdapter", "Could not parse date for UI: " + rawBackendDate);
            return rawBackendDate; // Trả về chuỗi gốc nếu không parse được
        }
    }
}