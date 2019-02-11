package cz.vakabus.reminderbot.endpoints.email;

import cz.vakabus.reminderbot.model.Identity;
import cz.vakabus.reminderbot.model.Message;
import cz.vakabus.reminderbot.endpoints.MessageEndpoint;
import jodd.mail.ReceivedEmail;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class EmailMessage implements Message {

    @NonNull ReceivedEmail email;
    @NonNull EmailEndpoint endpoint;

    public EmailMessage(@NonNull ReceivedEmail re, @NonNull EmailEndpoint endpoint) {
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

    @NonNull
    @Override
    public MessageEndpoint getSource() {
        return endpoint;
    }

    @NonNull
    @Override
    public Identity getSender() {
        return new EmailIdentity(email.from());
    }

    @Override
    public Date getSentDate() {
        return email.sentDate();
    }
}
