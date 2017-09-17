# Query Microservice

## Introduction

**/query** is a microservice for looking up author names and book titles. This endpoint is  not authenticated and thus doesn't need an Authorization HTTP header.

This uses the [openlibrary api](https://openlibrary.org/developers/api) to query for authors and title information.

## Spring Boot Application
The application listens on port 8080.

## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries USED

* [spring boot](https://projects.spring.io/spring-boot/) for REST framework.
* [gradle](https://gradle.org) for building.
* [spring fox](https://springfox.github.io/springfox/docs/current/) for Swagger documentation.

### Old google querying
Once you've obtained an API key, set it in a *.env* file next to the root *docker-compose.yml* with the format:
```
GOOGLE_API_KEY=A...yourkeyhere
```
This will set an environment variable *googleapikey* via *docker-compose* that then is injected by Spring into the controller.
