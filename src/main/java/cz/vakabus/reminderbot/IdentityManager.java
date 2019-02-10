package cz.vakabus.reminderbot;

import cz.vakabus.reminderbot.endpoints.EndpointsManager;
import cz.vakabus.reminderbot.model.Identity;
import cz.vakabus.reminderbot.model.Message;
import cz.vakabus.reminderbot.storage.User;
import cz.vakabus.reminderbot.utils.Result;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class IdentityManager {

    List<User> users;
    @Nullable
    transient HashMap<String, User> userMapping = null;

    private void rebuildCache() {
        if (userMapping != null)
            return;

        userMapping = new HashMap<>();

        for (User u : users) {
            assert !userMapping.containsKey(u.getName());
            userMapping.put(u.getName(), u);
            for (String alias : u.getAliases()) {
                assert !userMapping.containsKey(alias);
                userMapping.put(alias, u);
            }

            for (Set<String> ids : u.getContactInfo().values()) {
                for (String id : ids)
                    userMapping.put(id, u);
            }
        }
    }

    public IdentityManager(@NotNull String filename) {
        this.users = User.loadUsers(filename);
        rebuildCache();
    }

    boolean isKnown(@NotNull Identity identity) {
        return userMapping.containsKey(identity.getId());
    }

    @NotNull Result<Identity, String> parseIdentityTokens(@NotNull Message msg, @NotNull List<String> identityTokens) {
        if (identityTokens.isEmpty())
            return Result.success(msg.getSender());

        var text = String.join(" ", identityTokens);
        var idParts = text.split(" by ");

        if (idParts.length == 1) {
            // special cases
            switch (idParts[0]) {
                case "me":
                    return Result.success(msg.getSender());
            }

            var user = userMapping.get(text);
            if (user == null) return Result.error("No user named \"" + text + "\" found.");

            var contactId = user.getContactId(msg.getSource().getName());
            if (contactId.isEmpty())
                return Result.error("User has no configured contact using this method of communication.");

            return Result.success(new ParsedIdentity(contactId.get(), msg.getSource().getName()));

        } else if (idParts.length == 2) {
            var user = userMapping.get(idParts[0]);
            if (user == null) return Result.error("No user named \"" + text + "\" found.");

            var transportName = idParts[1].toLowerCase();
            if (!EndpointsManager.getInstance().isEndpointName(transportName))
                return Result.error("No messaging method named \"" + transportName + "\" found.");

            var contactId = user.getContactId(transportName);
            if (contactId.isEmpty())
                return Result.error("User has no configured contact using this method of communication.");

            return Result.success(new ParsedIdentity(contactId.get(), transportName));
        } else {
            return Result.error("Failed to parse identity of the user, to which I should deliver the reminder.");
        }
    }


    @Value
    private static class ParsedIdentity implements Identity {
        @NotNull String id;
        @NotNull String endpointName;
    }
}
