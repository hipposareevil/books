# Frontend Web Application

## Introduction

**/** is a single-page application written in [vue](https://vuejs.org/) using [bulma](http://bulma.io/) for style.

![Frontend](https://github.com/hipposareevil/books/blob/master/images/frontend/pngs/frontPage.png)


## Docker Images

There are two docker images for the frontend; _prod_ and _dev_. The _prod_ is essentially a single down compiled javascript file. Where _dev_ is designed to be run out of a mounted volume.

When running in _dev_ mode, the docker-compose.yml file should have this for the *frontend* segment:
~~~~
   # Frontend webpage + js
   frontend:
     container_name: frontend
     image: ${BOOK_REPOSITORY}books.frontend:dev
     restart: always
     logging:
       driver: "json-file"
     volumes:
      - "./images/frontend/content/mybooks/:/scratch/"
     networks:
       - booknet
~~~~
