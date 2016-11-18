Table of Contents
=================

   * [Books](#books)
      * [Endpoint(s)](#endpoints)
   * [Prerequisites](#prerequisites)
   * [Building](#building)
      * [Docker images used](#docker-images-used)
   * [Structure](#structure)
   * [Managing application](#managing-application)
      * [Running application](#running-application)
      * [Stopping application](#stopping-application)
   * [Accessing the application](#accessing-the-application)

# Books
Set of webservices to support a book repository (like goodreads.com or librarything.com). 

## Endpoint(s)
The following REST endpoints will be exposed wherever this application is run on port 8080.

Endpoint | Purpose
--- | ---
/author | Manage authors in database
/titles | **tbd** Manage titles/books in database
/query | **tbd** Microservice to query google|amazon for books and authors

# Prerequisites

* bash
* Docker (1.12) [install](https://docs.docker.com/engine/installation/)
* _docker-compose_ optional


# Building

There is 1 top level *build.sh* file that will build each container/micro-service and mysql database.
```
> ./build.sh
```


## Docker images used

These will be pulled automatically from dockerhub.com.

Docker image | Purpose
--- | ---
openjdk:8-jdk-alpine | Base image for web services
maven:3.3.9-jdk-8-alpine  | Used to build web services
nginx:latest | API Gateway
docker/compose:1.8.1 | Used to start/stop app

# Structure

Docker containers are used to house the Nginx frontend proxy (API Gateway), the backend database, and each of the microservices. All calls will be routed through Nginx and delegated to one of the microservices.

![Books Structure](https://github.com/hipposareevil/books/blob/master/structure.png)

# Managing application

The application is managed using docker-compose via a docker-compose.yml file. A convenience script is provided that runs docker-compose out of another container.

## Running application
```
> ./start.sh
```
or
```
> docker-compose -up -d
```


## Stopping application
```
> ./stop.sh
```
or
```
> docker-compose down
```

# Accessing the application

The app will be running on [localhost:8080](http://localhost:8080). It takes a few seconds for the MySQL database to come up..

Authors are listed via [localhost:8080/author](http://localhost:8080/author), while an individual author can be accessed via [localhost:8080/author/2](http://localhost:8080/author/2)




