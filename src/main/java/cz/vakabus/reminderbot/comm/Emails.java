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

    public static List<ReceivedEmail> downloadNewUnreadMessages(Instant newerThan) {
        LOGGER.info("Fetching emails...");

        var session = createImapServer().createSession();

        session.open();
        var emails = session
                .receive()
                .filter(EmailFilter.filter()
                        //.sentDate(EmailFilter.Operator.GE, newerThan.toEpochMilli())
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


        var server = ImapServer.create()
                .auth(config.getImapUsername(), config.getImapPassword())
                .host(config.getImapServerHostname())
                .ssl(config.isImapSSL())
                .port(config.getImapServerPort())
                .buildImapMailServer();

        return server;
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

        var email = Email.create()
                .to(receivedEmail.replyTo())
                .from(config.getEmailDisplayName(), config.getEmailAddress())
                .currentSentDate()
                .subject(receivedEmail.subject(), receivedEmail.subjectEncoding())
                .header("In-Reply-To", receivedEmail.messageId())
                .header("References", receivedEmail.header("References") + "\r\n " + receivedEmail.messageId())
                .textMessage("I should have reminded you about this...")
                .message(receivedEmail.messages());

        return email;
    }


    public static Email createParsingErrorEmail(ReceivedEmail receivedEmail) {
        Configuration config = Configuration.getInstance();

        var email = Email.create()
                .to(receivedEmail.replyTo())
                .from(config.getEmailDisplayName(), config.getEmailAddress())
                .currentSentDate()
                .subject(receivedEmail.subject(), receivedEmail.subjectEncoding())
                .header("In-Reply-To", receivedEmail.messageId())
                .header("References", receivedEmail.header("References") + "\r\n " + receivedEmail.messageId())
                .textMessage("Sorry, the datetime you specified could not be parsed fully... The reminder is NOT set.")
                .message(receivedEmail.messages());

        return email;
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
