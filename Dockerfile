FROM openjdk:11-slim

RUN mkdir /app
WORKDIR /app

COPY build/libs/reminderbot.jar /app

CMD while true; do java -jar reminderbot.jar; echo "Sleeping for 300sec before next run..."; sleep 300; done