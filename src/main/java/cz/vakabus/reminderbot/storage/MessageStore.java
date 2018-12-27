package cz.vakabus.reminderbot.storage;

import cz.vakabus.reminderbot.comm.Emails;
import cz.vakabus.reminderbot.utils.Json;
import jodd.mail.ReceivedEmail;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MessageStore {
    public static final Logger LOGGER = Logger.getLogger(MessageStore.class.getName());

    private MessageStore() {
        messages = new ArrayList<>();
        lastSuccessfulRun = Instant.now();
    }

    List<StoredMessage> messages;
    Instant lastSuccessfulRun;

    public List<StoredMessage> getMessages() {
        return messages;
    }

    public Instant getLastSuccessfulRun() {
        return lastSuccessfulRun;
    }

    public static MessageStore load(String filename) {
        LOGGER.info("Loading MessageStore from storage...");
        try {
            return Json.load(filename, MessageStore.class);
        } catch (IOException e) {
            LOGGER.warning("Failed to load messages! Assuming this is first run...");
            return new MessageStore();
        }
    }

    public void storeMessages(String filename) throws IOException {
        LOGGER.info("Storing MessageStore to disk...");
        Json.store(filename, this, MessageStore.class);
    }

    public void updateWithMessages(List<ReceivedEmail> emails) {
        LOGGER.info("Adding " + emails.size() + " new emails to the current messages in queue...");
        emails.stream()
                .map(Emails::serializeEmail)
                .map(msg -> new StoredMessage(CommunicationMethod.MAIL, msg))
                .forEach(messages::add);
    }

    public void setLastSuccessfulRun(Instant lastSuccessfulRun) {
        this.lastSuccessfulRun = lastSuccessfulRun;
    }

    public void setMessages(List<StoredMessage> messages) {
        this.messages = messages;
    }
}
