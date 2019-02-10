package cz.vakabus.reminderbot.model;

import cz.vakabus.reminderbot.endpoints.MessageEndpoint;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@Value
public class ParsedMessage {

    @NotNull Message message;
    @NotNull Instant time;

    @NotNull Identity deliverTo;

    @NotNull MessageEndpoint sink;
}
