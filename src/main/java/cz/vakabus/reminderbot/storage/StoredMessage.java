package cz.vakabus.reminderbot.storage;

import java.util.logging.Logger;

public class StoredMessage {
    CommunicationMethod type;
    String serializedMessage;

    public StoredMessage(CommunicationMethod type, String serializedMessage) {
        this.type = type;
        this.serializedMessage = serializedMessage;
    }

    public CommunicationMethod getType() {
        return type;
    }

    public String getSerializedMessage() {
        return serializedMessage;
    }
}
