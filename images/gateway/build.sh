#!/bin/bash

# Build nginx image

# image we build
image_name="books.gateway:latest"

# our real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# set build time
BUILD_TIME=$(date +%Y-%m-%dT%H:%M:%S%Z)
VERSION_TAG="latest"

then=$(date +%s)
echo "[[Building Docker image '$image_name']]"

# build image
docker build -t ${image_name} \
       --build-arg BUILD_TIME=${BUILD_TIME} \
       --build-arg VERSION=${VERSION_TAG} \
       "$our_directory" 
build_result=$?

# check result
now=$(date +%s)
elapsed=$(expr $now - $then)

if [ $build_result -eq 0 ]; then
    echo ""
    echo "[[Built $image_name in $elapsed second(s)]]"
else
    echo ""
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    echo "Unable to build Docker image for $image_name"
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    exit 1
fi
