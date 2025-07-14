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

    public CustomDateAdapter() {
        formats = Arrays.asList(
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'", Locale.US)
        );
        for (SimpleDateFormat f : formats) f.setTimeZone(TimeZone.getTimeZone("UTC"));
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
}