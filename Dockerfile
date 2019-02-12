FROM openjdk:11-slim

RUN apt-get update && apt-get install -y git

RUN mkdir /data
WORKDIR /data

CMD cd /;\
    rm -rf /reminderbot;\
    git clone https://github.com/vakabus/reminderbot.git;\
    cd /reminderbot;\
    ./gradlew --no-daemon shadowJar;\
    cd /data;\
    while true; do\
         java -jar /reminderbot/build/libs/reminderbot.jar;\
         echo "Sleeping for 300sec before next run...";\
         sleep 300;\
     done
