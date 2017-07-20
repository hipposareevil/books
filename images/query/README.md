# Intro

**Query** is microservice for querying the [google books api](https://developers.google.com/books/docs/v1/getting_started).

It is necessary to obtain a [google api key](https://support.google.com/cloud/answer/6158862) before using this endpoint.

# Set Google API key for application
Once you've obtained an API key, set it in a *.env* file next to the root docker-compose.yml with the format:
```
GOOGLE_API_KEY=A...yourkeyhere
```
This will set an environment variable *googleapikey* via docker-compose that then Spring injects into the controller.

# Spring Boot application
The application listens on port 8080

# Docker 
The Docker container will expose port 8080 

# Libraries used

* [spring boot](https://projects.spring.io/spring-boot/) for REST framework.
* [gradle](https://gradle.org) for building.
* [spring fox](https://springfox.github.io/springfox/docs/current/) for Swagger documentation.
* [google books](https://developers.google.com/books/docs/v1/getting_started) to query book titles.
