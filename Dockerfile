FROM maven:3.9.12-eclipse-temurin-25 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY application ./application

RUN mvn -q -DskipTests package

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /workspace/application/springboot/target/springboot-0.0.1-SNAPSHOT.jar /app/app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/app.jar"]
