# Tag Microservice

## Introduction

**/tag** is a microservice for creating, listing, updating and deleting Tags from the database. A tag can have any name and are visable among all users.  The *delete* and *put* operations are only available to the *admin* user.


## Supported calls
The list of supported calls and their documentation are available via the swagger endpoint. This runs on localhost:8080/swagger/ when the application is up.

## Fields for a Tag
A Tag entry has the following fields:

Field | Purpose
--- | ---
id | Unique ID of the Tag. This is used by the *user books* to map tags to a users individual bok.
name | Name of the tag. Examples: e-book, sci-fi, mystery, to-read.


## Authorization
It is necessary to authorize all REST calls to this endpoint. This is done by obtaining an authorization token from the */authorize* endpoint and adding it to the HTTP headees with the key *AUTHORIZATION*.  See [/authorize](https://github.com/hipposareevil/books/blob/master/images/authorize/README.md) for more information.


## Dropwizard Application
The application listens on port 8080.

## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries used

* [dropwizard](http://www.dropwizard.io/) for microservice framework.
* [maven](https://maven.apache.org/) for building.


