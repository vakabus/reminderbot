package cz.vakabus.reminderbot.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

public class Json {
    public static <T> T load(String filename, Type type) throws IOException {
        Gson gson = new Gson();
        try (JsonReader jsonReader = new JsonReader(new FileReader(new File(filename)))) {
            return (T) gson.fromJson(jsonReader, type);
        }
    }

    public static <T> void store(String filename, T data, Type type) throws IOException {
        Gson gson = new Gson();
        try (JsonWriter jsonWriter = new JsonWriter(new FileWriter(new File(filename)))) {
            gson.toJson(data, type, jsonWriter);
        }
    }
}
