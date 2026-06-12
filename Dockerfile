# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17-bookworm
WORKDIR /app

ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
ENV MAVEN_OPTS=-Xmx1024m

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src

RUN mvn -B -ntp exec:java \
      -Dexec.mainClass=com.microsoft.playwright.CLI \
      -Dexec.args="install --with-deps chromium" \
    && mvn -B -ntp test-compile -DskipTests

VOLUME ["/app/target"]

CMD ["tail", "-f", "/dev/null"]
