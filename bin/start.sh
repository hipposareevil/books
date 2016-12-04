#!/usr/bin/env bash

# Start the books application with docker-compose.
# If docker-compose doesn't exist, use the utils/docker-compose-run.sh script that was
# taken from https://docs.docker.com/compose/install/#/install-as-a-container

dc=docker-compose

if ! type "$dc" > /dev/null 2>&1 ; then
    dc=utils/docker-compose-run.sh
fi

# start up the containers and daemonize docker-compose
$dc up -d

echo ""
echo "Running 'books' on localhost:8080. Try http://localhost:8080/swagger/"
echo "Note: the app takes a few seconds to warm up while the DB initializes."
