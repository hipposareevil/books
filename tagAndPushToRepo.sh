#!/usr/bin/env bash

###################
# Script to tag all 'books' images and push them to a repository
# 
###################

########
# Set up variables
#
########
initialize_variables() {
    OUR_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

    # load in metadata from the .env file
    . ${OUR_DIRECTORY}/.env

    if [ -z $BOOK_REPOSITORY ]; then
        echo "Must set the BOOK_REPOSITORY environment variable"
        exit 1
    fi

    # Get version from webservice.version
    IMAGE_VERSION=$(cat ${OUR_DIRECTORY}/images/webservice.version)
}


############
# Tag and push an incoming name. It will be prepended with the
# ${BOOK_REPOSITORY} repository info.
############
tag_and_push() {
    what_to_tag=$1
    version_tag=$2

    image_id=$(docker images $what_to_tag | grep $version_tag | grep -v REPOSITORY | awk '{print $3}' | head -1)
    if [ -z "$image_id" ]; then
        echo "Unable to find $what_to_tag image"
        exit 1
    fi
    echo ""
    echo "Tagging and pushing '${image_id}'/'${what_to_tag}' as '${BOOK_REPOSITORY}${what_to_tag}:${version_tag}'"
    newtag=${BOOK_REPOSITORY}${what_to_tag}:${version_tag}
    docker tag ${image_id} ${newtag}
    docker push ${newtag}
}

############
# main
# 
############
main() {
    initialize_variables
    

    tag_and_push "books.author" $IMAGE_VERSION
    tag_and_push "books.authorize" $IMAGE_VERSION
    tag_and_push "books.book"  $IMAGE_VERSION
    tag_and_push "books.frontend"  $IMAGE_VERSION
    tag_and_push "books.frontend"  ${IMAGE_VERSION}-dev
    tag_and_push "books.gateway"  $IMAGE_VERSION
    tag_and_push "books.query"  $IMAGE_VERSION
    tag_and_push "books.review"  $IMAGE_VERSION
    tag_and_push "books.tag" $IMAGE_VERSION
    tag_and_push "books.user"  $IMAGE_VERSION
    tag_and_push "books.user_book"  $IMAGE_VERSION
}


# Call main
main "$@"
