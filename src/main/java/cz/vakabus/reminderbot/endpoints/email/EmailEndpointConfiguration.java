package cz.vakabus.reminderbot.endpoints.email;

import lombok.NonNull;
import lombok.Value;

@Value
public class EmailEndpointConfiguration {
    @NonNull String smtpServerHostname;
    int smtpServerPort;
    boolean smtpSSL;
    @NonNull String smtpUsername;
    @NonNull String smtpPassword;

    @NonNull String imapServerHostname;
    int imapServerPort;
    boolean imapSSL;
    @NonNull String imapUsername;
    @NonNull String imapPassword;

    @NonNull String emailDisplayName;
    @NonNull String emailAddress;
}
