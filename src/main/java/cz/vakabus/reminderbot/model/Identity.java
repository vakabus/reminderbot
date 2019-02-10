package cz.vakabus.reminderbot.model;

import cz.vakabus.reminderbot.endpoints.MessageEndpoint;

public interface Identity {
    String getEndpointName();
    String getId();
}
