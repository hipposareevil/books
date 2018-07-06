# Query Microservice

## Introduction

**/query** is a microservice for looking up author names and book titles. This endpoint is  not authenticated and thus doesn't need an Authorization HTTP header.

This uses the [openlibrary api](https://openlibrary.org/developers/api) to query for authors and title information.


## Go-kit Application
This uses go-kit for the framework and dep for the management of the dependencies (kindof like maven). A *vendor* directory will be created by dep in the *src/github.com/hipposareevil* sub-directory.

## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries used

* [go](https://golang.org/)
* [go-kit](https://github.com/go-kit/kit) - microservice framework.
* [dep](https://github.com/golang/dep) - depdendency management tool.


