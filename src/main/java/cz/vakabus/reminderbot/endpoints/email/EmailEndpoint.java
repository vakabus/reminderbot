package cz.vakabus.reminderbot.endpoints.email;

import cz.vakabus.reminderbot.endpoints.MessageEndpoint;
import cz.vakabus.reminderbot.model.Message;
import cz.vakabus.reminderbot.model.ParsedMessage;
import jodd.mail.*;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.val;

import jakarta.mail.Flags;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log
public class EmailEndpoint implements MessageEndpoint {
    static final String NAME = "mail";

    public static EmailEndpoint connect(EmailEndpointConfiguration config) {
        val imap = ImapServer.create()
                .auth(config.getImapUsername(), config.getImapPassword())
                .host(config.getImapServerHostname())
                .ssl(config.isImapSSL())
                .port(config.getImapServerPort())
                .buildImapMailServer();

        val smtp = SmtpServer.create()
                .host(config.getSmtpServerHostname())
                .auth(config.getSmtpUsername(), config.getSmtpPassword())
                .ssl(config.isSmtpSSL())
                .port(config.getSmtpServerPort())
                .property("mail.smtp.starttls.enable", "true")
                .buildSmtpMailServer();

        return new EmailEndpoint(imap.createSession(), smtp.createSession(), config.getEmailAddress(), config.getEmailDisplayName());
    }

    private EmailEndpoint(ReceiveMailSession receiveMailSession, SendMailSession sendMailSession, String email, String displayName) {
        this.receiveMailSession = receiveMailSession;
        this.sendMailSession = sendMailSession;
        this.emailAddress = email;
        this.displayName = displayName;
    }

    ReceiveMailSession receiveMailSession = null;
    SendMailSession sendMailSession = null;
    String emailAddress;
    String displayName;


    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Stream<Message> receive() {
        log.info("Fetching emails...");

        receiveMailSession.open();
        var emails = receiveMailSession
                .receive()
                .filter(EmailFilter.filter()
                        .flag(Flags.Flag.SEEN, false)
                )
                .unmark(Flags.Flag.SEEN)
                .get();

        receiveMailSession.close();

        log.info("" + emails.length + " emails fetched...");

        return Arrays.stream(emails).map(receivedEmail -> new EmailMessage(receivedEmail, this));
    }

    @Override
    public void markProcessed(Message message) {
        if (!(message instanceof EmailMessage))
            throw new UnsupportedOperationException("It's not possible to mark non-mail message read in email endpoint!");
        if (message.getSource() != this)
            throw new UnsupportedOperationException("It's not possible to mark message read which does not originate from here.");

        var msg = ((EmailMessage) message).email;
        receiveMailSession.open();
        msg.flags().add(Flags.Flag.SEEN);
        receiveMailSession.updateEmailFlags(msg);
        receiveMailSession.close();
    }

    @Override
    public void send(@NonNull ParsedMessage message) {
        assert message.getDeliverTo().getEndpointName().equals(this.getName());

        var email = Email.create()
                .to(message.getDeliverTo().getId())
                .from(displayName, emailAddress)
                .currentSentDate()
                .subject("Reminder")
                .textMessage("I was told to remind you about \"" + message.getRemindedObject() + "\"...\n\nYour ReminderBot\n\n" + quote(message.getMessage()));

        // add reference
        if (message.getMessage() instanceof EmailMessage) {
            var rcvdEmail = ((EmailMessage) message.getMessage()).email;
            email = email
                    .header("In-Reply-To", rcvdEmail.messageId())
                    .header("References", rcvdEmail.header("References") + "\r\n " + rcvdEmail.messageId())
                    .subject(rcvdEmail.subject(), rcvdEmail.subjectEncoding());
        }

        log.info("Sending emails...");
        sendMailSession.open();
        sendMailSession.sendMail(email);
        sendMailSession.close();
        log.info("EmailEndpoint sent...");
    }


    public static final String ERROR_MSG =
            "Sorry, Your reminder was not set. The REASON is:\n" +
                    "{reason}\n" +
                    "\n" +
                    "If you require more detailed explanation, please have a look at my GitHub\n" +
                    "profile at [https://github.com/vakabus/reminderbot]\n" +
                    "\n" +
                    "I hope, I will be more useful next time.\n" +
                    "Happy to serve You,\n" +
                    "            Your ReminderBot\n";

    private String quote(Message msg) {
        DateFormat sdf = SimpleDateFormat.getInstance();
        return "On " + sdf.format(msg.getSentDate()) + ", " + msg.getSender().getId() + " wrote:\n" +
                Arrays.stream(msg.getWholeText().split("\n")).map(ss -> "> " + ss).collect(Collectors.joining("\n"));
    }

    @Override
    public void reportError(@NonNull Message msg, @NonNull String error) {
        if (!(msg.getSender().getEndpointName().equals(getName())))
            throw new UnsupportedOperationException("Can't send message to unrelated message endpoints. This is JUST an email endpoint. Nothing more.");

        var email = Email.create();
        if (msg instanceof EmailMessage) {
            var receivedEmail = ((EmailMessage) msg).email;
            email = email
                    .to(receivedEmail.replyTo())
                    .from(displayName, emailAddress)
                    .currentSentDate()
                    .subject(receivedEmail.subject(), receivedEmail.subjectEncoding())
                    .header("In-Reply-To", receivedEmail.messageId())
                    .header("References", receivedEmail.header("References") + "\r\n " + receivedEmail.messageId());
        } else {
            email = email
                    .to(msg.getSender().getId())
                    .from(displayName, emailAddress)
                    .currentSentDate()
                    .subject("ReminderBot error notice");
        }

        email = email.textMessage(ERROR_MSG.replace("{reason}", error) + "\n\n" + quote(msg));

        sendMailSession.open();
        sendMailSession.sendMail(email);
        sendMailSession.close();
    }

}
