FROM    openjdk:17

LABEL   author="Roza Chatzigeorgiou"

VOLUME  /tmp

ENV APP_PORT=8080 DEBUG_PORT=5005

EXPOSE $APP_PORT $DEBUG_PORT

ARG JAR_FILE=target/money-transfer-0.0.1-SNAPSHOT.jar

ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app.jar"]
