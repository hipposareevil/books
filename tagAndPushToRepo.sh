#!/usr/bin/env bash

# Script to tag all 'books' images and push them to a repository

# load in metadata from the .env file
. .env

if [ -z $BOOK_REPOSITORY ]; then
    echo "Must set the BOOK_REPOSITORY environment variable"
    exit 1
fi

############
# Tag and push an incoming name. It will be prepended with the
# ${BOOK_REPOSITORY} repository info.
############
tag_and_push() {
    what_to_tag=$1

    image=$(docker images | grep -v $BOOK_REPOSITORY | grep $what_to_tag | awk '{print $3}')
    if [ -z "$image" ]; then
        echo "Unable to find $what_to_tag image"
        exit 1
    fi
    echo ""
    echo "Tagging $image as ${BOOK_REPOSITORY}$what_to_tag"
    docker tag $image ${BOOK_REPOSITORY}${what_to_tag}:latest
    docker push ${BOOK_REPOSITORY}${what_to_tag}:latest

}


# query
tag_and_push "books.query"

# author
tag_and_push "books.author"

# title
tag_and_push "books.title"

# nginx
tag_and_push "books.nginx"
