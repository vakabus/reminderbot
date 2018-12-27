package cz.vakabus.reminderbot.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Users {

    public static List<User> loadUsers(String filename) throws IOException {
        final var usersType = new TypeToken<List<User>>(){}.getType();

        Gson gson = new Gson();
        try (JsonReader jsonReader = new JsonReader(new FileReader(new File(filename)))) {
            return (List<User>) gson.fromJson(jsonReader, usersType);
        }
    }
}
