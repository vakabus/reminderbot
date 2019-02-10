package cz.vakabus.reminderbot.endpoints.email;

import cz.vakabus.reminderbot.model.Identity;
import cz.vakabus.reminderbot.model.Message;
import cz.vakabus.reminderbot.endpoints.MessageEndpoint;
import jodd.mail.ReceivedEmail;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class EmailMessage implements Message {

    @NotNull ReceivedEmail email;
    @NotNull EmailEndpoint endpoint;

    public EmailMessage(@NotNull ReceivedEmail re, @NotNull EmailEndpoint endpoint) {
        this.email = re;
        this.endpoint = endpoint;
    }

    private String getEmailText() {
        var messages = email.messages();
        var text = messages.stream().map(jodd.mail.EmailMessage::getContent).collect(Collectors.joining("\n"));
        return text;
    }


    @Override
    public String getWholeText() {
        return getEmailText();
    }

    @Override
    public String getCommand() {
        return getEmailText().split("\n")[0];
    }

    @Override
    public String getContent() {
        return Arrays.stream(getEmailText().split("\n")).skip(1).collect(Collectors.joining("\n"));
    }

    @NotNull
    @Override
    public MessageEndpoint getSource() {
        return endpoint;
    }

    @NotNull
    @Override
    public Identity getSender() {
        return new EmailIdentity(email.from());
    }

    @Override
    public Date getSentDate() {
        return email.sentDate();
    }
}
