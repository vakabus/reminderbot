package cz.vakabus.reminderbot.model;

import cz.vakabus.reminderbot.endpoints.MessageEndpoint;

import java.util.Date;

/**
 * General representation of received message by an communication endpoint.
 */
public interface Message {

    /**
     * @return Text of the whole message, including commands to the ReminderBot.
     */
    String getWholeText();

    /**
     * @return Part of the message, which is considered a command for ReminderBot.
     */
    String getCommand();

    /**
     * @return Part of the message, that is NOT considered a command. It's the whole message without the command.
     */
    String getContent();

    /**
     * @return MessageEndpoint which received this message.
     */
    MessageEndpoint getSource();

    /**
     * @return Identity representing the person, who sent this message.
     */
    Identity getSender();

    /**
     * @return Date object representing the time when the message was received. Used as a reference during parsing...
     */
    Date getSentDate();
}
