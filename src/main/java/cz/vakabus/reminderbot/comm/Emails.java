package cz.vakabus.reminderbot.comm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.sun.mail.imap.protocol.FLAGS;
import cz.vakabus.reminderbot.storage.Configuration;
import jodd.mail.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.activation.DataSource;
import javax.mail.Flags;
import javax.mail.Message;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Emails {
    public static final Logger LOGGER = Logger.getLogger(Emails.class.getName());
    public static final String ERROR_MSG =
            "Hi,\n" +
                    "\n" +
                    "I did NOT understand, what you sent me. Sorry. What I expect you to do is write\n" +
                    "me an email in a way I can understand. Specifically, that means, the first line\n" +
                    "will contain time specification. If I can't parse that, I bounce the email the\n" +
                    "same way, I did now. You can try my parsing abilities here:\n" +
                    "\n" +
                    "http://natty.joestelmach.com/try.jsp\n" +
                    "\n" +
                    "Hope I made it clear. Happy to serve you,\n" +
                    "Your ReminderBot\n" +
                    "      More about me at [https://github.com/vakabus/reminderbot]";

    public static List<ReceivedEmail> downloadUnreadMessages() {
        LOGGER.info("Fetching emails...");

        var session = createImapServer().createSession();

        session.open();
        var emails = session
                .receive()
                .filter(EmailFilter.filter()
                        .flag(Flags.Flag.SEEN, false)
                )
                .unmark(Flags.Flag.SEEN)
                .get();

        session.close();

        LOGGER.info("" + emails.length + " emails fetched...");

        return Arrays.asList(emails);
    }

    public static void markAsRead(List<ReceivedEmail> emails) {
        var session = createImapServer().createSession();
        session.open();

        emails.forEach(email -> email.flags().add(Flags.Flag.SEEN));
        emails.forEach(session::updateEmailFlags);

        session.close();
    }

    private static ImapServer createImapServer() {
        Configuration config = Configuration.getInstance();

        return ImapServer.create()
                .auth(config.getImapUsername(), config.getImapPassword())
                .host(config.getImapServerHostname())
                .ssl(config.isImapSSL())
                .port(config.getImapServerPort())
                .buildImapMailServer();
    }

    public static String serializeEmail(ReceivedEmail email) {
        Gson gson = new Gson();
        return gson.toJson(email, ReceivedEmail.class);
    }

    public static ReceivedEmail deserializeEmail(String string) {
        Gson gson = new Gson();
        return gson.fromJson(string, ReceivedEmail.class);
    }

    public static Optional<Instant> extractTime(ReceivedEmail receivedEmail) {
        Parser nattyParser = new Parser(TimeZone.getDefault());
        List<Date> dateList = receivedEmail.messages()
                .stream()
                .map(emailMessage -> emailMessage.getContent().trim())
                .filter(s -> !s.isEmpty())
                .map(s -> s.split("\n")[0].trim())
                .map(txt -> Jsoup.clean(txt, Whitelist.none()))
                .map(s -> nattyParser.parse(s, receivedEmail.sentDate()))
                .flatMap(Collection::stream)
                .filter(dateGroup -> (dateGroup.isDateInferred() || dateGroup.isTimeInferred()) && !dateGroup.isRecurring())
                .filter(dateGroup -> dateGroup.getText().equals(dateGroup.getFullText()))
                .flatMap(dateGroup -> dateGroup.getDates().stream())
                .collect(Collectors.toList());

        if (dateList.size() == 1) {
            return Optional.of(dateList.get(0).toInstant());
        } else {
            return Optional.empty();
        }
    }

    private static SmtpServer createSmtpServer() {
        Configuration config = Configuration.getInstance();

        return SmtpSslServer.create()
                .host(config.getSmtpServerHostname())
                .auth(config.getSmtpUsername(), config.getSmtpPassword())
                .ssl(config.isSmtpSSL())
                .port(config.getSmtpServerPort())
                .property("mail.smtp.starttls.enable", "true")
                .buildSmtpMailServer();
    }


    public static Email createReminderEmail(ReceivedEmail receivedEmail) {
        Configuration config = Configuration.getInstance();

        return Email.create()
                .to(receivedEmail.replyTo())
                .from(config.getEmailDisplayName(), config.getEmailAddress())
                .currentSentDate()
                .subject(receivedEmail.subject(), receivedEmail.subjectEncoding())
                .header("In-Reply-To", receivedEmail.messageId())
                .header("References", receivedEmail.header("References") + "\r\n " + receivedEmail.messageId())
                .textMessage("I should have reminded you about this...\n\nYour ReminderBot")
                .message(receivedEmail.messages());
    }


    public static Email createParsingErrorEmail(ReceivedEmail receivedEmail) {
        Configuration config = Configuration.getInstance();

        return Email.create()
                .to(receivedEmail.replyTo())
                .from(config.getEmailDisplayName(), config.getEmailAddress())
                .currentSentDate()
                .subject(receivedEmail.subject(), receivedEmail.subjectEncoding())
                .header("In-Reply-To", receivedEmail.messageId())
                .header("References", receivedEmail.header("References") + "\r\n " + receivedEmail.messageId())
                .textMessage(ERROR_MSG)
                .message(receivedEmail.messages());
    }

    public static void sendEmails(List<Email> emails) {
        LOGGER.info("Sending emails...");
        var session = createSmtpServer().createSession();
        session.open();
        emails.forEach(session::sendMail);
        session.close();
        LOGGER.info("Emails sent...");
    }

}
