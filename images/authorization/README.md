# Authorize Microservice

## Introduction

**/authorize** is a microservice for creating authorization tokens. Each token expires in 24 hours. The returned token should be placed into the HTTP Headers with key 'Authorization'.

Example usage:

* *admin* user creates user Bob with password "s3cret".
* Actual user Bob makes a REST call to */authorize* with:
```
{ "name":"bob",
   "password":"s3cret" }
```
* Bob receives a response with:
```
Bearer qwerty-1234-asdf-9876
```
* Bob inserts the following into the HTTP Headers for calls to any endpoint.
```
Authorization : Bearer qwerty-1234-asdf-9876
```

Example of *authorize* call via curl. This is slightly different than what the *swagger* UI shows:
```
$> curl -X POST http://localhost:8080/authorize/token --header 'Content-Type: application/json' -d '{"name":"bob, "password":"s3cret}'

```


## Supported calls
The list of supported calls and their documentation are available via the swagger endpoint. This runs on localhost:8080/swagger/ when the application is up.


## Dropwizard Application
The application listens on port 8080.

## Docker 
The Docker container will expose port 8080 to other containers on the *booknet* Docker network.

## Libraries used

* [dropwizard](http://www.dropwizard.io/) for microservice framework.
* [maven](https://maven.apache.org/) for building.


