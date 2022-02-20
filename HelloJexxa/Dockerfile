FROM ghcr.io/graalvm/graalvm-ce:latest
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["/app.jar"]