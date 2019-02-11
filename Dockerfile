FROM openjdk:11-slim

RUN apt-get update && apt-get install -y git

RUN mkdir /app
WORKDIR /

CMD git clone https://github.com/vakabus/reminderbot.git;\
    cd /reminderbot;\
    while true; do\
         ./gradlew run;\
          echo "Sleeping for 300sec before next run...";\
          sleep 300;\
     done
