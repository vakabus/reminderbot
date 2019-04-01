package cz.vakabus.reminderbot.endpoints;

import cz.vakabus.reminderbot.model.Message;
import cz.vakabus.reminderbot.model.ParsedMessage;
import lombok.NonNull;

import java.util.stream.Stream;

/**
 * Represents an communication channel which the ReminderBot uses.
 */
public interface MessageEndpoint {
    /**
     * Returns name of this communication channel. Is used during message parsing to determine
     * target communication channel.
     *
     * @return Name of this communication channel. Must NOT be null.
     */
    @NonNull String getName();

    /**
     * Connect to the communication channel and obtain a stream of messages to process. It should return only messages,
     * that have not been marked as processed.
     *
     * @return Stream of messages to process
     */
    Stream<Message> receive();

    /**
     * Mark some message as processed, so that the reminder is not send multiple times. The Message object will always
     * be the same as obtained from {@link MessageEndpoint#receive()} method.
     *
     * @param message message to mark as processed
     */
    void markProcessed(Message message);

    /**
     * Send a reminder based on information in parsed message passed in argument.
     *
     * @param message Message on which the reminder should be based...
     */
    void send(ParsedMessage message);

    /**
     * Called in case of an error during message processing. It's expected that this will send message to the user, so
     * that he/she knows, that something failed and the reminder will not arrive.
     *
     * @param msg Message object which caused the error
     * @param error Error message
     */
    void reportError(Message msg, String error);
}
