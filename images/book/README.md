# Book Microservice

## Introduction

**/book** is a microservice for querying, listing, adding and deleting Book entries from the database. Books are visable to all users but are only mutable by the *admin* user. 

## Supported calls
The list of supported calls and their documentation are available via the swagger endpoint. This runs on localhost:8080/swagger/ when the application is up.

## Fields for a Book
A Book entry has the following fields:

Field | Purpose
--- | ---
id | Unique ID of the author. This is used to manage the author and for reference in a Book.
name | Full name of the author. Example: "Isaac Asimov".
imageUrl | URL of image for the author. 


## Authorization
It is necessary to authorize all REST calls to this endpoint. This is done by obtaining an authorization token from the */authorize* endpoint and adding it to the HTTP headees with the key *AUTHORIZATION*.  See [/authorize](https://github.com/hipposareevil/books/blob/master/images/authorize/README.md) for more information.


## Dropwizard Application
The application listens on port 8080.

## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries used

* [dropwizard](http://www.dropwizard.io/) for microservice framework.
* [maven](https://maven.apache.org/) for building.

