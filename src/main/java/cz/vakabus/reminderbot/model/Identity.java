package cz.vakabus.reminderbot.model;

import cz.vakabus.reminderbot.endpoints.MessageEndpoint;

/**
 * Objects of this interface represent unique identifier for a user and a communication channel of his/her choice. These
 * objects are parsed out of received message and are primarily used to route reminders to the correct destination.
 */
public interface Identity {

    /**
     * @return a name of endpoint (communication channel) which should be used for sending
     */
    String getEndpointName();

    /**
     * @return an identifier/contact name on chosen communication channel. Identifies one person.
     */
    String getId();
}
