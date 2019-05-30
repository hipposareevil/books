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

    image=$(docker images $what_to_tag | grep -v REPOSITORY | awk '{print $3}')
    if [ -z "$image" ]; then
        echo "Unable to find $what_to_tag image"
        exit 1
    fi
    echo ""
    echo "Tagging and pushing '${image}' as '${BOOK_REPOSITORY}${what_to_tag}'"
    docker tag $image ${BOOK_REPOSITORY}${what_to_tag}
    docker push ${BOOK_REPOSITORY}${what_to_tag}
}



tag_and_push "books.author.go"
tag_and_push "books.authorize.go"
tag_and_push "books.book.go"
tag_and_push "books.frontend:dev"
tag_and_push "books.frontend:prod"
tag_and_push "books.gateway"
tag_and_push "books.query.go"
tag_and_push "books.review.go"
tag_and_push "books.tag.go"
tag_and_push "books.user.go"
tag_and_push "books.user_book.go"
