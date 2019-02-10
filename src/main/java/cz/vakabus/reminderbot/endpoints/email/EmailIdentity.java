package cz.vakabus.reminderbot.endpoints.email;

import cz.vakabus.reminderbot.model.Identity;
import jodd.mail.EmailAddress;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class EmailIdentity implements Identity {

    @NotNull  EmailAddress emailAddress;

    @NotNull
    @Override
    public String getEndpointName() {
        return EmailEndpoint.NAME;
    }

    @Override
    public String getId() {
        return emailAddress.getEmail();
    }
}
