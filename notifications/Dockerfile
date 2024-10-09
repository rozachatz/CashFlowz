FROM    openjdk:17

LABEL   author="Roza Chatzigeorgiou"

VOLUME /notification-service

ARG JAR_FILE=target/notifications-0.0.1-SNAPSHOT.jar

ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
