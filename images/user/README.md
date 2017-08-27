# User Microservice

## Introduction

**/user** is a microservice for creating, listing, updating and deleting Users from the database. The *admin* user is available to manage subsequent users. (It's recommended to change that *admin* users password.)

Expected usage:
* *admin* user creates user Bob with password "s3cret".
* Actual user Bob makes REST call to */authorize* with {"name":"bob","password","s3cret"} and recieves authorization token.
* That token is inserted into the HTTP Headers for calls to the other endpoints.

## Supported calls
The list of supported calls and their documentation are available via the swagger endpoint. This runs on localhost:8080/swagger/ when the application is up.

## Fields for a User
A User entry has the following fields:

Field | Purpose
--- | ---
id | Unique ID of the user. This is used to manage the user, obtain authorization, and link books to the user.
name | Name of the user. Example: "Bob D."


## Authorization
It is necessary to authorize all REST calls to this endpoint. This is done by obtaining an authorization token from the */authorize* endpoint and adding it to the HTTP headees with the key *AUTHORIZATION*.  See [/authorize](https://github.com/hipposareevil/books/blob/master/images/authorize/README.md) for more information.


## Dropwizard Application
The application listens on port 8080.

## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries used

* [dropwizard](http://www.dropwizard.io/) for microservice framework.
* [maven](https://maven.apache.org/) for building.


