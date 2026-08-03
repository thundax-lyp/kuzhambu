ARG KUZHAMBU_SERVER_BUILD_IMAGE=maven:3.9.11-eclipse-temurin-17
ARG KUZHAMBU_SERVER_RUNTIME_IMAGE=eclipse-temurin:17-jre

FROM ${KUZHAMBU_SERVER_BUILD_IMAGE} AS build

ARG STARTER_MODULE
ARG MAVEN_OPTS=

ENV MAVEN_OPTS=${MAVEN_OPTS}

WORKDIR /workspace/kuzhambu-servers
COPY kuzhambu-servers/ ./
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -pl starter/${STARTER_MODULE} -am -DskipTests package

FROM ${KUZHAMBU_SERVER_RUNTIME_IMAGE}

ARG STARTER_MODULE

ENV JAVA_OPTS=""

WORKDIR /app
COPY --from=build /workspace/kuzhambu-servers/starter/${STARTER_MODULE}/target/${STARTER_MODULE}-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
