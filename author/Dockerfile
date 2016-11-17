FROM openjdk:8-jdk-alpine

# setup env
RUN alias ll='ls -la'
RUN apk -U add --no-cache bash
RUN apk -U add --no-cache curl

# put jar and wait script in /opt/docker
RUN mkdir -p /opt/docker/

COPY target/author-1.0.jar /opt/docker/
COPY author.cfg.yml /opt/docker/
COPY waitforit.sh /opt/docker/

# wait for the DB at url booksdb:3306 for 240 seconds
ENTRYPOINT ["/opt/docker/waitforit.sh", "booksdb:3306", "240",   "java -jar /opt/docker/author-1.0.jar server /opt/docker/author.cfg.yml"]
