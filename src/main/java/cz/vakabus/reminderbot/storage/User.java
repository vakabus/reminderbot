package cz.vakabus.reminderbot.storage;

import com.google.gson.reflect.TypeToken;
import cz.vakabus.reminderbot.utils.Json;
import jodd.mail.EmailAddress;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class User {
    private static final Type USERS_TYPE = new TypeToken<List<User>>() {
    }.getType();

    String name;
    HashMap<CommunicationMethod, String> contactInfo;

    public static List<User> loadUsers(String filename) throws IOException {
        return Json.load(filename, USERS_TYPE);
    }

    public static void storeUsers(String filename, List<User> users) throws IOException {
        Json.store(filename, users, USERS_TYPE);
    }


    public static Predicate<EmailAddress> createEmailWhitelist(List<User> users) {
        var emails = users.stream()
                .filter(user -> user.contactInfo.containsKey(CommunicationMethod.MAIL))
                .map(user -> user.contactInfo.get(CommunicationMethod.MAIL))
                .collect(Collectors.toSet());

        return emailAddress -> emails.contains(emailAddress.getEmail());
    }
}
