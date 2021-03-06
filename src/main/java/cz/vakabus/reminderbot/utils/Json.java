package cz.vakabus.reminderbot.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.NonNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

public final class Json {

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T load(@NonNull String filename, @NonNull Type type) throws IOException {
        Gson gson = new Gson();
        try (JsonReader jsonReader = new JsonReader(new FileReader(new File(filename)))) {
            var r = (T) gson.fromJson(jsonReader, type);
            if (r == null) {
                throw new IOException("Could NOT parse JSON. Probably invalid syntax.");
            }
            return r;
        }
    }

    public static <T> void store(@NonNull String filename, T data, @NonNull Type type) throws IOException {
        Gson gson = new Gson();
        try (JsonWriter jsonWriter = new JsonWriter(new FileWriter(new File(filename)))) {
            gson.toJson(data, type, jsonWriter);
        }
    }
}
