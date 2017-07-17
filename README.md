
# Books
Set of webservices to support a book repository (like goodreads.com or librarything.com). 

# tldr
* build: *./books.sh build*
* run: *./books.sh start*
* url: *[http://localhost:8080/swagger/](http://localhost:8080/swagger/)*
* stop: *./books.sh stop*
* clean: *./books.sh clean*


# Endpoint(s)
Docker containers are used to house the Nginx frontend proxy (API Gateway), the backend database, and each of the microservices. All calls will be routed through the frontend proxy (API gateway) and delegated to one of the microservices. 

![Books Structure](https://github.com/hipposareevil/books/blob/master/diagrams/structure.png)

The following REST endpoints will be exposed wherever this application is run on port 8080.

Endpoint | Purpose
--- | ---
/author | Manage authors in database.
/title | Manage titles/books in database.
/query | Microservice to query google for books and authors.
/swagger/ | [swagger](http://swagger.io) documentation describing the REST apis.


## author
REST microservice managing authors; list, query, add, *delete*. Built with the [dropwizard](http://www.dropwizard.io/) framework.

## title
REST microservice managing book titles; list, query, add, *delete*. Built with the [dropwizard](http://www.dropwizard.io/) framework.

## query
REST microservice that queries google for new authors and book titles. Would be used by frontend to add new entries to application.
This service requires a google api key. See [query](https://github.com/hipposareevil/books/blob/master/query/README.md) for more information. 
When there is no google api key, this endpoint will just return empty results.  Built with the [spring boot](https://projects.spring.io/spring-boot/) framework.

## swagger
Swagger-ui that combines the swagger.yaml files from the REST endpoints. Uses [swagger-combine](https://hub.docker.com/r/hipposareevil/swagger-combine/) image to grab the definitions. This waits for the various endpoints to come up and then grabs the designated (in docker-compose.yml) yaml files, combines them and then serves up the endpoint via swagger-ui.

# Prerequisites

* bash
* Docker (1.12) [install here](https://docs.docker.com/engine/installation/)
* docker-compose _optional_


# Building

There is a top level script that builds, starts, and stops the application (and the corresponding microservices):
```
> ./books.sh build
```

Each microservice is built with either maven or gradle and uses a Docker container to do the complication. The containers use *.m2* and *.gradle* directories that cache the corresponding repositories.

## Docker images used

These will be pulled automatically from dockerhub.com.

Docker image | Purpose
--- | ---
docker/compose:1.8.1 | Used to start/stop application.
hipposareevil/swagger-combine | To combine and expose swagger documentation from each microservice.
hipposareevil/alpine-gradle | Used to build web services; *query*.
maven:3.3.9-jdk-8-alpine  | Used to build web services; author, title.
mysql:latest | MySQL database.
nginx:latest | Frontend API Gateway.
openjdk:8-jdk-alpine | Base image for web services. 

## Frameworks used

* [dropwizard](http://www.dropwizard.io/)
* [spring boot](https://projects.spring.io/spring-boot/)
* [swagger](http://swagger.io)

# Customizing application deployment
Docker compose utilizes an *.env* file where environment variables can be located. This project has the following variables.

## Google API key
The [query microservice](/images/query/) uses the google API service and requires a key. This should be put into the .env file.


## Docker image location
This defaults to using the local Docker repository, not a private registry. If you want to tag and push images to a private repository (or dockerhub), you can run the docker compose with the environment variable *BOOK_REPOSITORY*.

If your repository were at *mysuperrepo.com:5432*, you add the following to the *.env* file:
```
BOOK_REPOSITORY=mysuperrepo.com:5432
```
And then docker compose will utilize that for the root image locations. 

A utility script *tagAndPushToRepo.sh* is in the root directory and will tag all of the *books* images and push to the *BOOK_REPOSITORY* repository.

## Deployment Host name
This application defaults to *localhost* for the Swagger UI definition. When set to *localhost*, the UI can have trouble running example REST calls. For instance, if you're running the application on your hosted box *foo.com* and you access the UI via browser from your laptop, the queries will not go through. If you update the *.env* file with your host box name things will go smoother via Swagger.
```
DEPLOY_HOST_NAME=foo.com
```

See the [swagger-combine](https://github.com/hipposareevil/swagger-combine) project for more information on this variable.

# Managing application

The application is managed using docker-compose via a docker-compose.yml file. A convenience script is provided that runs docker-compose via a seperate container.

## Running application
```
> ./books.sh start
```
or
```
> docker-compose -up -d
```

## Stopping application
```
> ./books.sh stop
```
or
```
> docker-compose down
```

## Cleaning maven & gradle cache
```
> ./books.sh clean
```


# Accessing the application

The app will be running on [localhost:8080](http://localhost:8080). It takes a few seconds for the MySQL database to come up..

Swagger API description is at [localhost:8080/swagger/](http://localhost:8080/swagger/). This can be used to test each of the microservices

Authors are listed via [localhost:8080/author](http://localhost:8080/author), while an individual author can be accessed via [localhost:8080/author/2](http://localhost:8080/author/2)

Book titles are listed via [localhost:8080/title](http://localhost:8080/title), while an individual title can be accessed via [localhost:8080/title/2](http://localhost:8080/title/2)

The query micro-service is at [localhost:8080/query](http://localhost:8080/query). 

