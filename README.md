# ReminderBot

ReminderBot is slow responding chat bot which reminds you about things. It's inspiration was Slackbot's remind feature.
It supports multiple message transport, currently:

* email (id: `mail`)

You can interact with the bot by sending it a message. The message has to be in a predefined format. It's however designed
to feel like natural language. Messages to ReminderBot must look like this:

```
{identity specification} about {topic} {time specification}
Any text you like...
More text...
...
```

Also, the first line with the command can contain few filler words. So for example a real message might look like this:

```
please remind me by mail about mom's birthsday present at 30th May in the morning
              ^^^^^^^^^^       ^^^^^^^^^^^^^^^^^^^^^^^^^^ ^^^^^^^^^^^^^^^^^^^^^^^
               identity                topic                    timestamp
```

For more detailed explanation how the parsing works, see bellow.

## Build

### Local

_Java 11_ is **required**. If you don't have _Gradle 5_ installed, you can use `./gradlew` instead.

```sh
# to assemble executable jar file build/libs/reminderbot.jar
gradle shadowJar
# then it can be run by
java -jar build/libs/reminderbot.jar

# to build and run immediately
gradle run
```

### Production

Docker container is prepared for production. It runs the bot every 5minutes. You can just clone this repo and run:

```sh
docker build -t reminderbot .
docker run -d -v {PATH_TO_PERSISTENT_DATA}:/data --restart=always reminderbot:latest
``` 

To update the container, it should be sufficient to restart it, because on every start it fetches new code and rebuilds itself.
This is not a good way how to design Docker containers, but it works for me good enough and it means rapid development is possible.

## Configuration and data

In working directory of the application, there must be a file `registered_users.json` and in the directory `endpoints`
a configuration file for every installed messaging endpoint named `{endpoint id}.json`. So for email, the file would be named `mail.json`.

### Registered users

```json
[
  {"name":"User's Name","aliases":["name1", "alias1", "nick1"],"contactInfo":{"mail":["mail@example.com", "secondary_mail@example.com"]}},
  {"name":"Second User's Name","aliases":["name2", "alias2", "nick3"],"contactInfo":{"mail":["something@example.com", "something_else@example.com"]}}
] 

```

**WARNING**: every identifier in this file must be unique. There can't be a user with the same email address as someone
else. Also, there can't be an user named "mail@example.com", when there is other user having this email address configured
 as their contact info.
 
### Endpoint configuration

#### Email

The structure is self-explanatory:

```json
{
	"smtpServerHostname": "mail.server.host",
	"smtpServerPort": 587,
	"smtpSSL": false,
	"smtpUsername": "username",
	"smtpPassword": "pass",

	"imapServerHostname": "mail.server.host",
	"imapServerPort": 993,
	"imapSSL": true,
	"imapUsername": "username",
	"imapPassword": "pass",

	"emailDisplayName": "ReminderBot",
	"emailAddress": "reminderbot@mail.server.host"
}
```

## Command parsing

Parsing commands works in these steps:

0. extract first line from message
1. parse time using [Natty](http://natty.joestelmach.com/)
2. remove string representing the parsed time from command
3. skip leading filler words
4. until next filler word, consider everything as identity
5. after the filler word, consider everything as topic

As you can probably see, the parsing mechanism has its limitations and is definitely not perfect. Mainly, everything that
resembles a data will be parsed as a date regardless of its location in the command string.

### Identity parsing

There are 3 options:

1. no identity specified (command is only timestamp), then identity is user with the communication method,
that send the message.
2. identity does not contain "by", then method stays the same as the received message and user if looked up by the whole identity string
3. user is looked up by the text preceding "by", text after it is parsed as a communication method