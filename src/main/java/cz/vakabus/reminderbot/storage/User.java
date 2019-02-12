package cz.vakabus.reminderbot.storage;

import com.google.gson.reflect.TypeToken;
import cz.vakabus.reminderbot.utils.Json;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Value
public class User {
    private static final Type USERS_TYPE = new TypeToken<List<User>>() {
    }.getType();

    @NonNull String name;
    @NonNull Set<String> aliases;
    @NonNull HashMap<String, Set<String>> contactInfo;

    public Optional<String> getContactId(String endpointName) {
        if (contactInfo.containsKey(endpointName)) {
            return Optional.of(contactInfo.get(endpointName).iterator().next());
        } else {
            return Optional.empty();
        }
    }


    @NonNull
    public static List<User> loadUsers(@NonNull String filename) {
        try {
            return Json.load(filename, USERS_TYPE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse registered_users.json, file might be missing or syntax might not be valid.");
        }
    }

    public static void storeUsers(@NonNull String filename, List<User> users) throws IOException {
        Json.store(filename, users, USERS_TYPE);
    }


}
