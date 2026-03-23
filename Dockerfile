FROM gradle:8-jdk17-alpine AS build
WORKDIR /workspace
COPY . .
RUN gradle clean build --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /usr/app
COPY --from=build --chown=10001:10001 /workspace/build/libs/docker-exercises-project-1.0-SNAPSHOT.jar /usr/app/app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/app/app.jar"]