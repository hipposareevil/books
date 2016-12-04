Table of Contents
=================

   * [Books](#books)
   * [Endpoint(s)](#endpoints)
      * [author](#author)
      * [title](#title)
      * [swagger](#swagger)
      * [query](#query)
   * [Prerequisites](#prerequisites)
   * [Building](#building)
      * [Docker images used](#docker-images-used)
   * [Managing application](#managing-application)
      * [Running application](#running-application)
      * [Stopping application](#stopping-application)
   * [Accessing the application](#accessing-the-application)

# Books
Set of webservices to support a book repository (like goodreads.com or librarything.com). 

# tl;dr
* build: *bin/build.sh*
* run: *bin/start.sh*
* url: *[http://localhost](http://localhost:8080/swagger/)*
* stop: *bin/stop.sh*


# Endpoint(s)
Docker containers are used to house the Nginx frontend proxy (API Gateway), the backend database, and each of the microservices. All calls will be routed through the gateway and delegated to one of the microservices.

![Books Structure](https://github.com/hipposareevil/books/blob/master/images/structure.png)

The following REST endpoints will be exposed wherever this application is run on port 8080.

Endpoint | Purpose
--- | ---
/author | Manage authors in database
/title | Manage titles/books in database
/swagger/ | [swagger](http://swagger.io) documentation describing the REST apis
/query | **tbd** Microservice to query google|amazon for books and authors

## author
REST microservice managing authors; list, query, add, *delete*. Uses [dropwizard](http://www.dropwizard.io/) as the framework.

## title
REST microservice managing book titles; list, query, add, *delete*. Uses [dropwizard](http://www.dropwizard.io/) as the framework.

## swagger
Swagger-ui that combines the swagger.yaml files from the REST endpoints. Uses [swagger-combine](https://hub.docker.com/r/hipposareevil/swagger-combine/) image to grab the definitions. This waits for the various endpoints to come up and then grabs the designated (in docker-compose.yml) yaml files, combines them and then serves up the endpoint via swagger-ui.

## query
*(TBD)* REST microservice that queries google for new authors and book titles. Would be used by frontend to add new entries to application. 


# Prerequisites

* bash
* Docker (1.12) [install here](https://docs.docker.com/engine/installation/)
* docker-compose _optional_


# Building

There is 1 top level *bin/build.sh* file that will build each container/microservice and mysql database.
```
> ./bin/build.sh
```

The build uses a docker container to do the maven compliation. This leaves a *.m2* directory in the root directory, which is shared between all of the maven projects.


## Docker images used

These will be pulled automatically from dockerhub.com.

Docker image | Purpose
--- | ---
docker/compose:1.8.1 | Used to start/stop app
hipposareevil/swagger-combine | To expose swagger documentation
maven:3.3.9-jdk-8-alpine  | Used to build web services
mysql:latest | Mysql database
nginx:latest | API Gateway
openjdk:8-jdk-alpine | Base image for web services


# Managing application

The application is managed using docker-compose via a docker-compose.yml file. A convenience script is provided that runs docker-compose out of another container.

## Running application
```
> ./bin/start.sh
```
or
```
> docker-compose -up -d
```

## Stopping application
```
> ./bin/stop.sh
```
or
```
> docker-compose down
```

# Accessing the application

The app will be running on [localhost:8080](http://localhost:8080). It takes a few seconds for the MySQL database to come up..

Authors are listed via [localhost:8080/author](http://localhost:8080/author), while an individual author can be accessed via [localhost:8080/author/2](http://localhost:8080/author/2)

Swagger description is at [localhost:8080/swagger/](http://localhost:8080/swagger/).




