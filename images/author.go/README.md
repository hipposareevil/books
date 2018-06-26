#  Microservice

## Introduction

**/author** is a microservice for querying, listing, adding and deleting Author entries from the database. Authors are visable to all users but are only mutable by the *admin* user. 

## Encryption

Password are encrypted with [bcrypt](https://godoc.org/golang.org/x/crypto/bcrypt).

## Supported calls
The list of supported calls and their documentation are available via the swagger endpoint. This runs on localhost:8080/swagger/ when the application is up.

## Fields for an Author
An Author entry has the following fields:

Field | Purpose
--- | ---
id | Unique ID of the author. This is used to manage the author and for reference in a Book.
name | Full name of the author. Example: "Isaac Asimov".
imageUrl | URL of image for the author. 


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
