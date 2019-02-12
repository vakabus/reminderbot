/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package cz.vakabus.reminderbot;

import cz.vakabus.reminderbot.endpoints.EndpointsManager;
import cz.vakabus.reminderbot.endpoints.email.EmailEndpoint;
import cz.vakabus.reminderbot.endpoints.email.EmailEndpointConfiguration;
import cz.vakabus.reminderbot.model.Message;
import cz.vakabus.reminderbot.model.ParsedMessage;
import cz.vakabus.reminderbot.utils.Json;
import cz.vakabus.reminderbot.utils.Pair;
import cz.vakabus.reminderbot.utils.Result;
import lombok.extern.java.Log;

import java.io.IOException;
import java.time.Instant;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Overview of functionality:
 *
 * This app is supposed to be run at regular interval. It's not a daemon.
 *
 * 1. load users
 * 2. load stored messages and last success time
 * 3. init communication channels - open connections
 * 4. fetch new messages (newer then last success time) and store them (just in memory)
 * 5. filter out already processed messages
 * 6. for all stored messages, check if reminder should be send and send if necessary
 * 7. write all messages to disk
 * 8. write current time to disk
 *
 * In case of any error, bail out and stop the world. We might send a reminder twice, but must never loose one.
 */
@Log
public class App {
    static Instant startupTime;

    public static void main(String[] args) throws IOException {
        log.info("ReminderBot starting up...");
        startupTime = Instant.now();

        log.info("Loading IdentityManager...");
        IdentityManager idManager = new IdentityManager("registered_users.json");

        log.info("Loading message endpoint configuration...");
        var emailConfig = Json.<EmailEndpointConfiguration>load("endpoints/mail.json", EmailEndpointConfiguration.class);

        log.info("Initializing message endpoints...");
        EndpointsManager.getInstance().registerEndpoint(EmailEndpoint.connect(emailConfig));

        log.info("Starting message processing...");
        var messageStream = EndpointsManager.getInstance().downloadAllMessages();

        var parser = new Parser();
        Stream<Result<ParsedMessage, Pair<Message, String>>> parsingResultStream = messageStream.map(message -> {
            log.fine("Parsing message...");
            var parserResult = parser.parseMessage(message, idManager);
            if (parserResult.isSuccess()) {
                log.info("OK - Message parsed successfully...");
                return Result.success(parserResult.unwrap());
            } else {
                log.info("ERR - Failed to parse message...");
                return Result.error(Pair.of(message, parserResult.unwrapError()));
            }
        });

        var parsedMessageStream = parsingResultStream.flatMap(parsedMessagePairResult -> {
            if (parsedMessagePairResult.isSuccess()) {
                return Stream.of(parsedMessagePairResult.unwrap());
            } else {
                log.info("ERR - Sending error report back...");
                var error = parsedMessagePairResult.unwrapError();
                error.getFirst().getSource().reportError(error.getFirst(), error.getSecond());
                error.getFirst().getSource().markProcessed(error.getFirst());
                return Stream.empty();
            }
        });

        parsedMessageStream = parsedMessageStream.flatMap(parsedMessage -> {
            log.fine("Checking sender authorization....");
            if (idManager.isKnown(parsedMessage.getMessage().getSender())) {
                log.info("OK - Sender is authorised to use this service...");
                return Stream.of(parsedMessage);
            } else {
                log.info("ERR - Sender is NOT authorised to use this service...");
                var endpoint = parsedMessage.getMessage().getSource();
                endpoint.reportError(parsedMessage.getMessage(), "You are unauthorised to use this service.");
                endpoint.markProcessed(parsedMessage.getMessage());
                return Stream.empty();
            }
        });


        parsedMessageStream.forEach(parsedMessage -> {
            if (parsedMessage.getTime().isAfter(startupTime)) {
                // future reminder, currently nothing to do
                log.info("OK - Reminder is still in future. Doing nothing.");
                return;
            }

            //send reminder
            log.info("OK - Sending reminder...");
            var endpoint = parsedMessage.getSink();
            endpoint.send(parsedMessage);
            parsedMessage.getMessage().getSource().markProcessed(parsedMessage.getMessage());
        });

        log.info("ReminderBot terminating...");
    }
}
