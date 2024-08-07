# Build stage
FROM docker.io/gradle:8.8.0 AS temp_build
ARG SKIP_TESTS=false
COPY build.gradle settings.gradle /home/gradle/src/
COPY src /home/gradle/src/src
COPY config /home/gradle/src/config
COPY gradle /home/gradle/src/gradle
WORKDIR /home/gradle/src

RUN if [ "$SKIP_TESTS" = "true" ]; then \
    gradle build --no-daemon -x test; \
  else \
    gradle build --no-daemon; \
  fi

# Runtime stage
# Windows
#FROM openjdk:17-alpine
# MacOs M1
FROM bellsoft/liberica-openjdk-alpine-musl:17
RUN addgroup -S nonroot \
    && adduser -S nonroot -G nonroot
USER nonroot
WORKDIR /app
COPY --from=temp_build /home/gradle/src/build/libs/*.jar /app/srs-api.jar
ENTRYPOINT ["java", "-jar", "/app/srs-api.jar"]
