ARG KUZHAMBU_SERVER_BUILD_IMAGE=maven:3.9.11-eclipse-temurin-17
ARG KUZHAMBU_SERVER_RUNTIME_IMAGE=eclipse-temurin:17-jre
ARG KUZHAMBU_MYSQL_CLIENT_IMAGE=mysql:8.4

FROM ${KUZHAMBU_MYSQL_CLIENT_IMAGE} AS mysql-client

FROM ${KUZHAMBU_SERVER_BUILD_IMAGE} AS build

ARG STARTER_MODULE
ARG MAVEN_OPTS=

ENV MAVEN_OPTS=${MAVEN_OPTS}

WORKDIR /workspace/kuzhambu-servers
COPY kuzhambu-servers/ ./
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -pl starter/${STARTER_MODULE} -am -DskipTests package

FROM ${KUZHAMBU_SERVER_RUNTIME_IMAGE}

ARG STARTER_MODULE

# Operations backup and restore scripts execute inside the starter container.
# Copy the MySQL command-line tools from their official image so builds do not
# depend on a distro package mirror being reachable from the deployment host.
COPY --from=mysql-client /usr/bin/mysql /usr/local/bin/mysql
COPY --from=mysql-client /usr/bin/mysqldump /usr/local/bin/mysqldump
COPY --from=mysql-client /lib64/libncurses.so.6 /usr/local/lib/libncurses.so.6
RUN ldconfig

ENV JAVA_OPTS=""

WORKDIR /app
COPY --from=build /workspace/kuzhambu-servers/starter/${STARTER_MODULE}/target/${STARTER_MODULE}-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
