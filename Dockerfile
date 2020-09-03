FROM gradle:6.6-jdk14 as build
WORKDIR /sample
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src
COPY conf conf
RUN gradle shadowJar

FROM openjdk:14.0.2-slim-buster
WORKDIR /sample
COPY --from=build /sample/build/libs/sample-1.0.0-all.jar app.jar
COPY conf conf
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
