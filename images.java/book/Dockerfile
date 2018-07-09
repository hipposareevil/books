FROM openjdk:8-jdk-alpine

# Args passed in via 'docker build'
# Used by the LABELs
ARG BUILD_TIME
ARG VERSION

# setup env
RUN alias ll='ls -la' && \
    apk -U add --no-cache bash curl && \
# put jar and wait script in /opt/docker
    mkdir -p /opt/docker/

COPY target/book-1.0.jar /opt/docker/
COPY book.cfg.yml /opt/docker/
COPY waitforit.sh /opt/docker/

# Putting LABEL last so we can re-use the preceding caching layers
LABEL org.label-schema.build-date="$BUILD_TIME" \
      org.label-schema.vendor="github.com/hipposareevil" \
      org.label-schema.version="$VERSION" \
      org.label-schema.description="Microservice for managing books in the project." \
      org.label-schema.name="books.book" 

# wait for the DB at url books_db:3306 for 240 seconds
ENTRYPOINT ["/opt/docker/waitforit.sh", "books_db:3306", "240",   "java -Xmx64m -jar /opt/docker/book-1.0.jar server /opt/docker/book.cfg.yml"]
