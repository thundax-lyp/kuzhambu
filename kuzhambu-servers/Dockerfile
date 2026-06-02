FROM maven:3.9.11-eclipse-temurin-17 AS build

ARG STARTER_MODULE

WORKDIR /workspace/kuzhambu-servers
COPY kuzhambu-servers/ ./
RUN mvn -pl starter/${STARTER_MODULE} -am -DskipTests package

FROM eclipse-temurin:17-jre

ENV JAVA_OPTS=""

WORKDIR /app
COPY --from=build /workspace/kuzhambu-servers/starter/${STARTER_MODULE}/target/${STARTER_MODULE}-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
