# Introduction

This is the frontend proxy (API Gateway) for the set of microservices. It has a simple *nginx.conf* configuration file that sets up *upstream* servers for each microservice.  There are two custom HTML pages; 502.html and swagger.yaml.

# Included resources

## 502.html
This page is returned when an upstream server is not ready. In general, this happens when the database is not up yet, which causes the dropwizard programs to error out.

## swagger.yaml
This page is passed into the *swagger* container/endpoint, which combines microservice definitions into one usable end web page. The page containers overrides for *info* and *hostname* variables.
