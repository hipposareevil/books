FROM openjdk:8-jdk-alpine

# setup env
RUN alias ll='ls -la'

RUN mkdir -p /opt/docker
COPY  build/libs/query-1.0.jar /opt/docker

ENTRYPOINT ["java", "-Xmx64m", "-jar",  "/opt/docker/query-1.0.jar"]
