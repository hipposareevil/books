#!/usr/bin/env bash

# Download and start Redis and MySQL. Do MySQL first as it's slow to start up
# these will both be exposed to the localhost/127.0.0.1

echo "Downloading and running mysql"
docker run --name db -p 3306:3306 -e MYSQL_USER=booksuser -e MYSQL_PASSWORD=books -e MYSQL_ROOT_PASSWORD=booksit -e MYSQL_DATABASE=booksdatabase -d mysql:latest

echo "mysql started on 127.0.0.1:3306"
echo ""

echo "Downloading and starting Redis"
docker run --name redis -p 6379:6379 -d redis:3.0-alpine
echo "redis started on  127.0.0.1:6379"
echo ""
