# User Book Microservice

## Introduction

**/user_book** is a microservice for creating, listing, updating and deleting *user books* from the database. Each *user book* is a link between a *User* and a *Book*.

For example, user Bob has two *user books* in his *to read* list. He would create a *user book* for each of those books and add the tag *to read* to each book.


## Supported calls
The list of supported calls and their documentation are available via the swagger endpoint. This runs on localhost:8080/swagger/ when the application is up.

## Fields for a User Book
A User Book entry has the following fields:

Field | Purpose
--- | ---
id | Unique ID of the user book. 
rating | 1 for thumbs up, 0 for thumbs down.
tags | list of Tags for this user book.
bookId | ID of Book. Used in the */book* endpoint.
userId | ID of the User. Used in the */user* endpoint.
data | Optional metadata from the user, e.g. notes on the book.

## Authorization
It is necessary to authorize all REST calls to this endpoint. This is done by obtaining an authorization token from the */authorize* endpoint and adding it to the HTTP headees with the key *AUTHORIZATION*.  See [/authorize](https://github.com/hipposareevil/books/blob/master/images/authorize/README.md) for more information.


## Dropwizard Application
The application listens on port 8080.

## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries used

* [dropwizard](http://www.dropwizard.io/) for microservice framework.
* [maven](https://maven.apache.org/) for building.


