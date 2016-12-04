#!/usr/bin/env bash

# Stops the books application with docker-compose.
# If docker-compose doesn't exist, use the utils/docker-compose-run.sh script that was
# taken from https://docs.docker.com/compose/install/#/install-as-a-container

dc=docker-compose

if ! type "$dc" > /dev/null 2>&1 ; then
    dc=utils/docker-compose-run.sh
fi

# start up the containers and daemonize docker-compose
$dc down

echo "Stopped 'books' app"
