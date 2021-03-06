#  User Book Microservice

## Introduction

**/user_book** is a microservice for creating, listing, updating and deleting *user books* from the database. Each *user book* is a link between a *User* and a *Book*.

For example, user Bob has two *user books* in his *to read* list. He would create a *user book* for each of those books and add the tag *to read* to each book

## Supported calls
The list of supported calls and their documentation are available via the swagger endpoint. This runs on localhost:8080/swagger/ when the application is up.


## Authorization
It is necessary to authorize all REST calls to this endpoint. This is done by obtaining an authorization token from the */authorize* endpoint and adding it to the HTTP headees with the key *AUTHORIZATION*.  See [/authorize](https://github.com/hipposareevil/books/blob/master/images/authorize/README.md) for more information.

## Go-kit Application
This uses go-kit for the framework and dep for the management of the dependencies (kindof like maven). A *vendor* directory will be created by dep in the *src/github.com/hipposareevil* sub-directory.


## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries used

* [go](https://golang.org/)
* [go-kit](https://github.com/go-kit/kit) - microservice framework.
* [dep](https://github.com/golang/dep) - depdendency management tool.
* [bcrypt](https://godoc.org/golang.org/x/crypto/bcrypt) - encryption library
