#!/usr/bin/env bash

# Build query  project via gradle,
# then build docker image

# image we build
imageName="books.query:latest"

# our real directory (so this can be called from outside directories)
ourDirectory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

###
# build project via gradle using a docker container
# volumes:
# ourDirectory ==> /work          (source)
# .gradle      ==> /GRADLE_CACHE  (gradle repo cache)
docker run --rm \
       -it \
       -v "$ourDirectory":/work \
       -v "$ourDirectory"/../../.gradle:/GRADLE_CACHE \
       hipposareevil/alpine-gradle \
       build


if [ $? -eq 0 ]; then
    $ourDirectory/buildImage.sh
else
    echo ""
    echo "Unable to build project for books.query"
    exit 1
fi
