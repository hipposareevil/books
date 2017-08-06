FROM openjdk:8-jdk-alpine

# setup env
RUN alias ll='ls -la'
RUN apk -U add --no-cache bash curl

# put jar and wait script in /opt/docker
RUN mkdir -p /opt/docker/

COPY target/tag-1.0.jar /opt/docker/
COPY tag.cfg.yml /opt/docker/
COPY waitforit.sh /opt/docker/

# wait for the DB at url books_db:3306 for 240 seconds
ENTRYPOINT ["/opt/docker/waitforit.sh", "books_db:3306", "240",   "java -jar /opt/docker/tag-1.0.jar server /opt/docker/tag.cfg.yml"]
