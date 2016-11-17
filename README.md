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
* docker-compose [install using containers](https://docs.docker.com/compose/install/#/install-as-a-container)


# Building

There is 1 top level *build.sh* file that will build each container/micro-service and mysql database.
```
> ./build.sh
```


## Docker images used

These will be pulled automatically from dockerhub.com.

Docker image | Purpose
--- | ---
openjdk:8-jdk-alpine | base image for web services
maven:3.3.9-jdk-8-alpine  | used to build web services
nginx:latest | API Gateway

# Structure

Docker containers are used to house the Nginx frontend proxy (API Gateway), the backend database, and each of the microservices. All calls will be routed through Nginx and delegated to one of the microservices.

![Books Structure](https://github.com/hipposareevil/books/blob/master/structure.png)

# Running application

The application is started with
```
> docker-compose up -d
```
# Stopping application

The application is stopped with
```
> docker-compose down
```

# Accessing the application

The app will be running on [localhost:8080](http://localhost:8080). It takes a few seconds for the MySQL database to come up..

Authors are listed via [localhost:8080/authors](http://localhost:8080/authors), while an individual author can be accessed via [localhost:8080/authors/2](http://localhost:8080/authors/2)




