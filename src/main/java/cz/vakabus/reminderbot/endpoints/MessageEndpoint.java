package cz.vakabus.reminderbot.endpoints;

import cz.vakabus.reminderbot.model.Message;
import cz.vakabus.reminderbot.model.ParsedMessage;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface MessageEndpoint {
    @NotNull String getName();
    Stream<Message> receive();
    void markProcessed(Message message);
    void send(ParsedMessage message);
    void reportError(Message msg, String error);
}
