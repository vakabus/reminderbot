package cz.vakabus.reminderbot.endpoints.email;

import cz.vakabus.reminderbot.model.Identity;
import jodd.mail.EmailAddress;
import lombok.NonNull;
import lombok.Value;

@Value
public class EmailIdentity implements Identity {

    @NonNull  EmailAddress emailAddress;

    @NonNull
    @Override
    public String getEndpointName() {
        return EmailEndpoint.NAME;
    }

    @Override
    public String getId() {
        return emailAddress.getEmail();
    }
}
