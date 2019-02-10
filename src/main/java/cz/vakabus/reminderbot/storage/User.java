package cz.vakabus.reminderbot.storage;

import com.google.gson.reflect.TypeToken;
import cz.vakabus.reminderbot.utils.Json;
import lombok.SneakyThrows;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

@Value
public class User {
    private static final Type USERS_TYPE = new TypeToken<List<User>>() {
    }.getType();

    @NotNull String name;
    @NotNull Set<String> aliases;
    @NotNull HashMap<String, Set<String>> contactInfo;

    public Optional<String> getContactId(String endpointName) {
        if (contactInfo.containsKey(endpointName)) {
            return Optional.of(contactInfo.get(endpointName).iterator().next());
        } else {
            return Optional.empty();
        }
    }


    @NotNull
    @SneakyThrows
    public static List<User> loadUsers(@NotNull String filename) {
        return Json.load(filename, USERS_TYPE);
    }

    public static void storeUsers(@NotNull String filename, List<User> users) throws IOException {
        Json.store(filename, users, USERS_TYPE);
    }


}
