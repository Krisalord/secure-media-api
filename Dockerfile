FROM gradle:8.5-jdk21 AS build
WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY src/ src/

RUN chmod +x ./gradlew
# 1. CHANGE: Use buildFatJar instead of build
RUN ./gradlew buildFatJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar app.jar
COPY --from=build /app/src/main/resources/application.conf ./application.conf
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar -port=${PORT:-8080} -config=application.conf"]