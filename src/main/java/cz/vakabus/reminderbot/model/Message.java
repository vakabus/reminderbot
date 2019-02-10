package cz.vakabus.reminderbot.model;

import cz.vakabus.reminderbot.endpoints.MessageEndpoint;

import java.util.Date;

public interface Message {
    String getWholeText();
    String getCommand();
    String getContent();
    MessageEndpoint getSource();
    Identity getSender();
    Date getSentDate();
}
