FROM eclipse-temurin:25.0.2_10-jre-alpine@sha256:f10d6259d0798c1e12179b6bf3b63cea0d6843f7b09c9f9c9c422c50e44379ec

ENV USER_NAME api-user
ENV APP_HOME /opt/$USER_NAME/app
ENV SPRING_PROFILES_ACTIVE=default

# we can use -XX:MaxRAMPercentage=85 if we control the memory from the container engine
ENV JVM_HEAP_SETTINGS="-Xmx256m"

ENV JAVA_RUN_OPTS="-Dfile.encoding=UTF-8 \
-Djava.net.preferIPv4Stack=true \
-XX:+UseParallelGC \
-XshowSettings:all \
-XX:+PrintFlagsFinal"

# This variable can be used in runtime to set or override JVM settings. For example:
# docker run --rm -ti -e JAVA_OPTS="-Xms50M -Xmx50M" my-app-image
ENV JAVA_OPTS=""

# Add a volume pointing to /tmp, used by Tomcat
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 --start-period=30s --start-interval=3s \
  CMD wget_output=$(wget --timeout=1 --tries=1 --server-response --spider http://127.0.0.1:8080/api/actuatorz/info 2>&1); \
      echo "$wget_output"; \
      echo "$wget_output" | awk '/^  HTTP/{code=$2} END{exit (code == 401 ? 0 : 1)}'

# Create the user that will run the application
RUN adduser -D --shell /bin/bash --home /opt/$USER_NAME $USER_NAME
RUN mkdir -p $APP_HOME

# Add the application's jar to the container
ADD target/*.jar $APP_HOME/application.jar
RUN chown -R $USER_NAME: $APP_HOME

USER $USER_NAME
WORKDIR $APP_HOME

# Run the jar file. Keep $JAVA_OPTS as the last option before -jar
CMD java -Djava.security.egd=file:/dev/./urandom $JVM_HEAP_SETTINGS $JAVA_RUN_OPTS $JAVA_OPTS -jar application.jar
