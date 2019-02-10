package cz.vakabus.reminderbot;

import cz.vakabus.reminderbot.endpoints.EndpointsManager;
import cz.vakabus.reminderbot.model.Identity;
import cz.vakabus.reminderbot.model.Message;
import cz.vakabus.reminderbot.model.ParsedMessage;
import cz.vakabus.reminderbot.utils.Result;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {
    @NotNull
    private static final HashSet<String> FILLER_WORDS;

    static {
        FILLER_WORDS = new HashSet<>();
        Stream.of("please", "remind", "on", "at", "in", "about").forEach(FILLER_WORDS::add);
    }


    @NotNull Result<ParsedMessage, String> parseMessage(@NotNull Message message, @NotNull final IdentityManager identityManager) {
        val command = message.getCommand().strip();
        val nattyParser = new com.joestelmach.natty.Parser(TimeZone.getDefault());
        val formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

        val dates = nattyParser.parse(command, message.getSentDate());
        if (dates.size() != 1) {
            return Result.error("No datetime specification found in command. Sorry.");
        }

        val dg = dates.get(0);
        if (dg.getDates().size() != 1) {
            String msg = "Datetime is ambiguous. There are multiple possible results."
                    + dg.getDates().stream().map(formatter::format).map(s -> "* " + s).collect(Collectors.joining("\n"))
                    + "\n";
            return Result.error(msg);
        }

        val instant = dg.getDates().get(0).toInstant();
        val commandWithoutDateTokenizer = new StringTokenizer(command.replace(dg.getText(), ""));

        // drop all filler words from beginning
        while (commandWithoutDateTokenizer.hasMoreTokens()) {
            String token = commandWithoutDateTokenizer.nextToken();
            if (!FILLER_WORDS.contains(token))
                break;
        }

        // then expect identity tokens until first filler word
        var identityTokens = new ArrayList<String>();
        while (commandWithoutDateTokenizer.hasMoreTokens()) {
            String token = commandWithoutDateTokenizer.nextToken();
            if (FILLER_WORDS.contains(token))
                break;

            identityTokens.add(token);
        }

        // then all everything else is [about]
        var about = new ArrayList<String>();
        while (commandWithoutDateTokenizer.hasMoreTokens())
            about.add(commandWithoutDateTokenizer.nextToken());
        String aboutText = about.isEmpty() ? "something" : String.join(" ", about);


        val iden = identityManager.parseIdentityTokens(message, identityTokens);
        if (iden.isError())
            return Result.error(iden.unwrapError());

        val endpoint = EndpointsManager.getInstance().getEndpointBy(iden.unwrap().getEndpointName());
        return Result.success(new ParsedMessage(message, instant, iden.unwrap(), aboutText, endpoint.orElseThrow()));
    }
}
