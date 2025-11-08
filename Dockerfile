FROM gradle:jdk24 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:24-jre-alpine
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/moderationbot.jar /app/moderationbot.jar
ENTRYPOINT ["java","-jar","/app/moderationbot.jar"]
