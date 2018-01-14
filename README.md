Table of Contents
=================

   * [Introduction](#introduction)
   * [tldr](#tldr)
   * [Build, Run, and Access](#build-run-and-access)
      * [Prerequisites](#prerequisites)
      * [Docker images used](#docker-images-used)
      * [Frameworks used](#frameworks-used)
      * [Managing application](#managing-application)
         * [Running application](#running-application)
         * [Stopping application](#stopping-application)
         * [Cleaning maven &amp; gradle cache](#cleaning-maven--gradle-cache)
      * [Accessing the application](#accessing-the-application)
      * [Customizing application deployment](#customizing-application-deployment)
         * [Docker image location](#docker-image-location)
         * [Deployment Host name](#deployment-host-name)
   * [Implementation](#implementation)
      * [Endpoints](#endpoints)
         * [(frontend)](#frontend)
         * [query](#query)
         * [author](#author)
         * [book](#book)
         * [user](#user)
         * [user_book](#user_book)
         * [tag](#tag)
         * [tag.go](#taggo)
         * [authorize](#authorize)
         * [swagger](#swagger)
      * [Databases](#databases)
         * [MySQL](#mysql)
         * [Redis](#redis)
            * [Authentication](#authentication)
            * [Cache](#cache)
      * [Metrics](#metrics)
   * [Notes and Thoughts](#notes-and-thoughts)
      * [Initial](#initial)
      * [Memory Usage](#memory-usage)
      * [Framework Changes](#framework-changes)
      * [Plans](#plans)
# Introduction
This project has two goals; to create a book repository (like [goodreads.com](http://goodreads.com) or [librarything.com](http://librarything.com)), and to experiment with various microservice frameworks, golang, java, docker, swagger, and javascript.

The frontend web application is a [SPA](https://en.wikipedia.org/wiki/Single-page_application) written in Javascript ([vue.js](https://vuejs.org/)).

The backend is a set of micro-services written in Java and golang, backed by [mysql](https://www.mysql.com/) and [redis](https://redis.io/) databases.  There are various (micro-service) endpoints for interfacing with and querying the data, see [endpoints](#endpoints) below. Most endpoints requires authorization, which is obtained at the */authorize* endpoint. See the */swagger* endpoint for full information.  

There is an initial user of *admin* with same password. The *admin* user can create a new user, or update the *admin* user via the */user* endpoint or the main web application. 

# tldr
* build: *./books.sh build*
* run: *./books.sh start*
* swagger url: *[http://localhost:8080/swagger/](http://localhost:8080/swagger/)*
* frontend url: *[http://localhost:8080/](http://localhost:8080/)*
* stop: *./books.sh stop*
* clean: *./books.sh clean*

# Build, Run, and Access

There is a top level script that builds, starts, and stops the application (and the corresponding microservices):
```
> ./books.sh build
```

Each microservice is built with either maven or gradle and uses a Docker container to do the complication. The containers use *.m2* and *.gradle* directories that cache the corresponding repositories.

## Prerequisites

* bash
* Docker (17.09) ([install here](https://docs.docker.com/engine/installation/))
* docker-compose _optional_


## Docker images used

These will be pulled automatically from dockerhub.com.

Docker image | Purpose
--- | ---
docker/compose:1.8.1 | Used to start/stop application.
hipposareevil/alpine-dep  | For golang dependency management.
hipposareevil/swagger-combine | To combine and expose swagger documentation from each microservice.
hipposareevil/alpine-gradle | Used to build web services; *query*.
maven:3.3.9-jdk-8-alpine  | Used to build web services; author, title.
mysql:latest | MySQL database.
redis:3.0-alpine | Redis K/V database.
nginx:latest | API Gateway.
openjdk:8-jdk-alpine | Base image for web services. 


## Frameworks used

* [dropwizard](http://www.dropwizard.io/)
* [spring boot](https://projects.spring.io/spring-boot/)
* [go-kit](https://github.com/go-kit/kit)
* [swagger](http://swagger.io)
* [bulma css](http://bulma.io/)
* [vue javascript](https://vuejs.org/)
* [graphite](https://graphiteapp.org)
* [grafana](https://grafana.com)


## Managing application

The application is managed using docker-compose via a docker-compose.yml file. A convenience script is provided that runs docker-compose via a separate container.

### Running application
```
> ./books.sh start
```
or
```
> docker-compose -up -d
```

### Stopping application
```
> ./books.sh stop
```
or
```
> docker-compose down
```

### Cleaning maven & gradle cache
```
> ./books.sh clean
```


## Accessing the application

The web application runs at [localhost:8080/](http://localhost:8080/). It takes a few seconds for the MySQL database to come up.

Swagger API description is at [localhost:8080/swagger/](http://localhost:8080/swagger/). This can be used to test each of the microservices


## Customizing application deployment
Docker compose utilizes an *.env* file where environment variables can be located. This project has the following variables.

### Docker image location
This defaults to using the local Docker repository, not a private registry. If you want to tag and push images to a private repository (or dockerhub), you can run the docker compose with the environment variable *BOOK_REPOSITORY*.

If your repository were at *mysuperrepo.com:5432*, you add the following to the *.env* file:
```
BOOK_REPOSITORY=mysuperrepo.com:5432
```
And then docker compose will utilize that for the root image locations. 

A utility script *tagAndPushToRepo.sh* is in the root directory and will tag all of the *books* images and push to the *BOOK_REPOSITORY* repository.

### Deployment Host name
This application defaults to *localhost* for the Swagger UI definition. When set to *localhost*, the UI can have trouble running example REST calls. For instance, if you're running the application on your hosted box *foo.com* and you access the UI via browser from your laptop, the queries will not go through. If you update the *.env* file with your host box name things will go smoother via Swagger.
```
DEPLOY_HOST_NAME=foo.com
```

See the [swagger-combine](https://github.com/hipposareevil/swagger-combine) project for more information on this variable.


# Implementation

Notes on implementation.

## Endpoints
The web application, each microservice endpoint, and backend database are contained in Docker containers. All containers are attached to the Docker network *books_booknet*.

All endpoints are exposed to the outside world via an API _gateway_ which runs in a separate container and listens on port 8080.

![Books Structure](https://github.com/hipposareevil/books/blob/master/diagrams/structure.png)

The following endpoints are proxied via the API Gateway on port 8080 (configurable via the *docker-compose.yml* file).

Endpoint | Purpose
--- | ---
/ | Frontend single-page application.
/query | Microservice to query openlibrary.org for book titles and authors.
/author | Manage list of authors in database.
/book | Manage list of books in database. 
/user | Manage users.
/user_book | Manage books for a user. 
/tag | Manage tags.
/authorize | Authorize access to endpoints.
/swagger/ | [swagger](http://swagger.io) documentation describing the REST APIs.

### (frontend) 
Frontend web application at **/**. This uses the microservices to manage the books, authors, users, user lists, and tags. Utilizes the [vue](https://vuejs.org/) framework and [bulma](https://bulma.io/) framework.

See [frontend](https://github.com/hipposareevil/books/blob/master/images/frontend/README.md) for more information. 


### query
REST microservice that queries openlibrary for new authors and book titles. Would be used by frontend to add new entries to application.

See [query](https://github.com/hipposareevil/books/blob/master/images/query/README.md) for more information. 


### author
Microservice to manage the complete set of Authors in the database. Operations include: add, list all, list single and delete.

See [author](https://github.com/hipposareevil/books/blob/master/images/author/README.md) for more information. 

### book
Microservice to manage the complete set of Books in the database. Operations include; list, query, add, delete.

See [book](https://github.com/hipposareevil/books/blob/master/images/book/README.md) for more information. 

### user
Microservice to manage users. A *user* is used to maintain a set of *user books*, which stores which books the user is cataloging, along with metadata, tags and a rating. In addition, a *user* is used to obtain an authorization token for authenticating against the various endpoints.

See [user](https://github.com/hipposareevil/books/blob/master/images/user/README.md) for more information. 

### user_book
Microservice to manage a set of books for a user. Each user has a list of books they can catalog. Each *user book* has a link to the real Book and associated Author. In addition, a *user book* has user *data* and a set of *tags*. 

See [user_book](https://github.com/hipposareevil/books/blob/master/images/user_book/README.md) for more information. 


### tag
Microservice to manage tags. Tags can be applied to a user's set of books via the *user_books* endpoint.  Multiple tags may be applied to a single book, e.g. "e-book" and "sci-fi".

*Note:* This is now implemented in Go instead of Java.

See [tag](https://github.com/hipposareevil/books/blob/master/images/tag.go/README.md) for more information. 


### authorize
Microservice to authenticate a user. This creates a token of the form 'Bearer qwerty-1234-asdf-9876', which is then added to the headers of any calls to the other endpoints.

See [authorize](https://github.com/hipposareevil/books/blob/master/images/authorization/README.md) for more information. 

### swagger
Swagger-ui that combines the swagger.yaml files from the REST endpoints. Uses [swagger-combine](https://hub.docker.com/r/hipposareevil/swagger-combine/) image to grab the definitions.

This waits for the various endpoints to come up and then grabs the designated (in docker-compose.yml) yaml files, combines them and then serves up the endpoint via swagger-ui.

## Databases
There are two databases used to manage the books and users. The data is stored in the *database* directory. 

### MySQL
[MySQL](https://www.mysql.com/) is used to store books, authors, users, tags and user books lists.

The database schema is stored in *database/initial/mybooks.sql*

See [mysql](https://github.com/hipposareevil/books/blob/master/mysql/README.md) for more information and method to update the *admin* user's password.

### Redis

[Redis](https://redis.io/) is used to store key/value pairs for the services.

#### Authentication
Authentication tokens are created by the *authorize* service and stored in Redis. Redis is then used by all services to verify authentication.

#### Cache
Redis is also used as a cache for the services. When a service makes a REST call to another service, the calling service stores the returned data in the cache. For example, the *book* service calls the *author* service to get the author's name for a book. The *book* service stores that in the cache for the next call. The *author* service will flush the cache when mutations to the author database have been made.

## Metrics

Metrics are provided via [graphite](https://graphiteapp.org/) and [grafana](https://grafana.com/).

See [metrics](https://github.com/hipposareevil/books/blob/master/metrics/README.md) for more information.

# Notes and Thoughts

## Initial
I created this project to experiment with writing multiple micro-services and wiring them together in a docker environment. I started using [dropwizard](http://www.dropwizard.io/) for a Java based server. The ramp up time was fairly quick and I was able to easily do most tasks with little pain. I played with [spring boot](https://projects.spring.io/spring-boot/) as well, but stuck with dropwizard for the rest of the services.

## Memory Usage
After I had 6 services implemented, I ran everything on my hosted environment and saw that I was running out of memory. I checked my _docker stats_ and my *micro* services were each using 250MB.  I lowered my Java memory usage with -*Xmx64m* for each service and the usage dropped to 180MB. This was so much (close to 2GB ram) that I ended up buying more memory to run this project on [linode](https://linode.com). Which is ridiculous and not very **micro**.

I faintly remembered someone else looking into the memory usage of Java applications, especially on Docker, but I had ignored it thinking that it wouldn't affect me.
Here [is the analysis blog](http://trustmeiamadeveloper.com/2016/03/18/where-is-my-memory-java/)

## Framework Changes
As I wanted this to be a set of micro-services that had a small footprint instead of jumbo, I looked into other micro-service frameworks. I looked at [node](https://nodejs.org/en/) and [go](https://golang.org/) but node looked to have a large memory footprint from other people's analysis and looking at my *frontend* javascript app (which uses node for hot swapping).  I ran a golang application in Docker and the memory usage was minimal so I wrote a new micro-service using [go-kit](https://github.com/go-kit/kit).

I re-wrote *tag* using go-kit and had the following size changes:
Metric | golang | java | go % of java
--- | --- | --- | ---
Image size| 7.56MB | 136MB | 5.5%
Memory | 2MB | 187MB | 1.1%

The golang service's image size is 5% of the Java implementation.
The more striking difference is the golang service's 1.1% memory usage compared to the Java implementation.

*caveat* The image size is not always an accurate measurement due to image layer sharing (all the java images share the same base image, so the accumulated size is closer to 140MB than ~900MB (7 * 130MB)).

## Plans

* Port services from Java to golang for memory usage.
* Investigate [istio.io](https://istio.io/) for service discovery and metrics.
