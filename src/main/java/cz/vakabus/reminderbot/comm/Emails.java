package cz.vakabus.reminderbot.comm;

import com.google.gson.Gson;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import cz.vakabus.reminderbot.storage.Configuration;
import cz.vakabus.reminderbot.utils.Result;
import jodd.mail.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.mail.Flags;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
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
                    "      More about me at [https://github.com/vakabus/reminderbot]\n" +
                    "\n" +
                    "------------------------------------------------------------------------------\n" +
                    "DETAILED REASON:\n";

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

    public static Result<Instant, String> extractTime(ReceivedEmail receivedEmail) {
        Parser nattyParser = new Parser(TimeZone.getDefault());
        List<Result<Instant, String>> dateList = receivedEmail.messages()
                .stream()
                .map(emailMessage -> emailMessage.getContent().trim())
                .filter(s -> !s.isEmpty())
                .map(s -> s.split("\n")[0].trim())
                .map(txt -> Jsoup.clean(txt, Whitelist.none()))
                .map(s -> nattyParser.parse(s, receivedEmail.sentDate()))
                .flatMap(Collection::stream)
                .map((Function<DateGroup, Result<Instant, String>>) dateGroup -> {
                    if (dateGroup.getText().equals(dateGroup.getFullText())) {
                        if (dateGroup.isRecurring()) {
                            return Result.error("Parsed date is recurring, that's not supported.");
                        } else if (dateGroup.getDates().size() == 1) {
                            return Result.success(dateGroup.getDates().get(0).toInstant());
                        } else {
                            return Result.error("From the supplied message, multiple dates were parsed. I'm not sure, which one is the correct one.");
                        }
                    } else {
                        var formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
                        String msg = "\"" + dateGroup.getText() + "\" is the part of Your message that was understood. "
                                + "I think it means all of these (might be empty):\n\n"
                                + dateGroup.getDates().stream().map(formatter::format).map(s -> "* " + s).collect(Collectors.joining("\n"))
                                + "\n\n";
                        return Result.error(msg);
                    }
                })
                .collect(Collectors.toList());

        if (dateList.size() == 1) {
            return dateList.get(0);
        } else {
            var nlist = dateList.stream().filter(Result::isSuccess).collect(Collectors.toList());
            if (nlist.size() == 1) {
                return nlist.get(0);
            } else {
                return Result.error("Ambiguous date specification. Found too many possible candidates. Here are their errors:" + dateList.stream().filter(Result::isError).map(Result::unwrapError).collect(Collectors.joining("\n\n--------\n\n")));
            }
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


    public static Email createParsingErrorEmail(ReceivedEmail receivedEmail, String errorMsg) {
        Configuration config = Configuration.getInstance();

        return Email.create()
                .to(receivedEmail.replyTo())
                .from(config.getEmailDisplayName(), config.getEmailAddress())
                .currentSentDate()
                .subject(receivedEmail.subject(), receivedEmail.subjectEncoding())
                .header("In-Reply-To", receivedEmail.messageId())
                .header("References", receivedEmail.header("References") + "\r\n " + receivedEmail.messageId())
                .textMessage(ERROR_MSG + errorMsg)
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
