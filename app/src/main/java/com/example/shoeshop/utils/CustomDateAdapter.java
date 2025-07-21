package com.example.shoeshop.utils;

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
            new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

    public CustomDateAdapter() {
        formats = Arrays.asList(
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        );
        for (SimpleDateFormat f : formats) f.setTimeZone(TimeZone.getTimeZone("UTC"));
        UI_OUTPUT_FORMAT.setTimeZone(TimeZone.getDefault());
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
    public static String formatBackendDateForUI(String rawBackendDate) {
        if (rawBackendDate == null || rawBackendDate.isEmpty()) {
            return "N/A";
        }

        List<SimpleDateFormat> parsers = Arrays.asList(
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'", Locale.US)
        );
        for (SimpleDateFormat p : parsers) {
            p.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        Date parsedDate = null;
        for (SimpleDateFormat parser : parsers) {
            try {
                parsedDate = parser.parse(rawBackendDate);
                if (parsedDate != null) break;
            } catch (ParseException ignored) {
            }
        }

        if (parsedDate != null) {
            return UI_OUTPUT_FORMAT.format(parsedDate);
        } else {
            android.util.Log.w("CustomDateAdapter", "Could not parse date for UI: " + rawBackendDate);
            return rawBackendDate;
        }
    }
}