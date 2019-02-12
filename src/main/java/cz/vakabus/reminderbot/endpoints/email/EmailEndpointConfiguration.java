package cz.vakabus.reminderbot.endpoints.email;

import cz.vakabus.reminderbot.utils.Json;
import lombok.NonNull;
import lombok.Value;

import java.io.IOException;

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

    public static EmailEndpointConfiguration loadConfiguration() {
        try {
            return Json.load("endpoints/mail.json", EmailEndpointConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse email endpoint configuration...");
        }
    }
}
