FROM openjdk:8-jdk-alpine

# Args passed in via 'docker build'
# Used by the LABELs
ARG BUILD_TIME
ARG VERSION

# setup env
RUN alias ll='ls -la' && \
    mkdir -p /opt/docker

COPY build/libs/query-1.0.jar /opt/docker

# Putting LABEL last so we can re-use the preceding caching layers
LABEL org.label-schema.build-date="$BUILD_TIME" \
      org.label-schema.vendor="github.com/hipposareevil" \
      org.label-schema.version="$VERSION" \
      org.label-schema.description="Microservice for querying openlibrary for books & authors." \
      org.label-schema.name="books.query" 


ENTRYPOINT ["java", "-Xmx64m", "-jar",  "/opt/docker/query-1.0.jar"]
