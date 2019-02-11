FROM openjdk:11-slim

RUN apt-get update && apt-get install -y git

RUN mkdir /app
WORKDIR /

CMD git clone https://github.com/vakabus/reminderbot.git;\
    cd /reminderbot;\
    ./gradlew --no-daemon shadowJar;\
    while true; do\
         java -jar build/libs/reminderbot.jar;\
         echo "Sleeping for 300sec before next run...";\
         sleep 300;\
     done
