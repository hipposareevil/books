# Tag Microservice

## Introduction

**/user** is a microservice for creating, listing, updating and deleting Users from the database. The *admin* user is available to manage subsequent users. (It's recommended to change that *admin* users password.)

Expected usage:
* *admin* user creates user Bob with password "s3cret".
* Actual user Bob makes REST call to */authorize* with {"name":"bob","password","s3cret"} and recieves authorization token.
* That token is inserted into the HTTP Headers for calls to the other endpoints.

## Encryption

Password are encrypted with [bcrypt](https://godoc.org/golang.org/x/crypto/bcrypt).


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

## Go-kit Application
This uses go-kit for the framework and dep for the management of the dependencies (kindof like maven). A *vendor* directory will be created by dep in the *src/github.com/hipposareevil* sub-directory.


## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries used

* [go](https://golang.org/)
* [go-kit](https://github.com/go-kit/kit) - microservice framework.
* [dep](https://github.com/golang/dep) - depdendency management tool.
* [bcrypt](https://godoc.org/golang.org/x/crypto/bcrypt) - encryption library
