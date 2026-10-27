FROM gradle:8.5-jdk21 AS build
WORKDIR /app

COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew installDist --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/install/*/lib ./lib
EXPOSE 8080

ENTRYPOINT ["java", "-cp", "lib/*", "io.ktor.server.netty.EngineMain"]