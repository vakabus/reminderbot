package cz.vakabus.reminderbot.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class Configuration {
    private static Logger LOGGER = Logger.getLogger(Configuration.class.getName());
    private static Configuration singleton = null;

    public static Configuration getInstance() {
        if (singleton != null)
            return singleton;

        LOGGER.info("Loading configuration from disk...");
        Gson gson = new Gson();
        try (JsonReader jsonReader = new JsonReader(new FileReader(new File("config.json")));) {
            singleton = gson.fromJson(jsonReader, Configuration.class);
            return singleton;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load configuration!!!", e);
        }
    }

    public String getSmtpServerHostname() {
        return smtpServerHostname;
    }

    public int getSmtpServerPort() {
        return smtpServerPort;
    }

    public boolean isSmtpSSL() {
        return smtpSSL;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public String getImapServerHostname() {
        return imapServerHostname;
    }

    public int getImapServerPort() {
        return imapServerPort;
    }

    public boolean isImapSSL() {
        return imapSSL;
    }

    public String getImapUsername() {
        return imapUsername;
    }

    public String getImapPassword() {
        return imapPassword;
    }

    String smtpServerHostname;
    int smtpServerPort;
    boolean smtpSSL = true;
    String smtpUsername;
    String smtpPassword;

    String imapServerHostname;
    int imapServerPort;
    boolean imapSSL = true;
    String imapUsername;
    String imapPassword;

    String emailDisplayName;
    String emailAddress;

    public String getEmailDisplayName() {
        return emailDisplayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
