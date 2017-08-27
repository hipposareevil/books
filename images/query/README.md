# Query Microservice

## Introduction

**/query** is a microservice for querying the [google books api](https://developers.google.com/books/docs/v1/getting_started). The endpoint is not authenticated and thus doesn't need an Authorization HTTP header.

It is necessary to obtain a [google api key](https://support.google.com/cloud/answer/6158862) before using this endpoint.

## Set Google API key for application
Once you've obtained an API key, set it in a *.env* file next to the root *docker-compose.yml* with the format:
```
GOOGLE_API_KEY=A...yourkeyhere
```
This will set an environment variable *googleapikey* via *docker-compose* that then is injected by Spring into the controller.

## Spring Boot Application
The application listens on port 8080.

## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries USED

* [spring boot](https://projects.spring.io/spring-boot/) for REST framework.
* [gradle](https://gradle.org) for building.
* [spring fox](https://springfox.github.io/springfox/docs/current/) for Swagger documentation.
* [google books](https://developers.google.com/books/docs/v1/getting_started) to query for authors and book titles.
