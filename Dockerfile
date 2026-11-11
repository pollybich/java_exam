FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
ARG MAVEN_OPTS
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    --mount=type=secret,id=java_cacerts,target=/tmp/java-cacerts \
    mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/media-portal-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
USER 10001
ENTRYPOINT ["java", "-jar", "app.jar"]
