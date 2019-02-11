package cz.vakabus.reminderbot.model;

import cz.vakabus.reminderbot.endpoints.MessageEndpoint;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class ParsedMessage {

    @NonNull Message message;
    @NonNull Instant time;

    @NonNull Identity deliverTo;
    @NonNull String remindedObject;

    @NonNull MessageEndpoint sink;
}
