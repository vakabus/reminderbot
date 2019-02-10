package cz.vakabus.reminderbot.endpoints.email;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class EmailEndpointConfiguration {
    @NotNull String smtpServerHostname;
    int smtpServerPort;
    boolean smtpSSL;
    @NotNull String smtpUsername;
    @NotNull String smtpPassword;

    @NotNull String imapServerHostname;
    int imapServerPort;
    boolean imapSSL;
    @NotNull String imapUsername;
    @NotNull String imapPassword;

    @NotNull String emailDisplayName;
    @NotNull String emailAddress;
}
