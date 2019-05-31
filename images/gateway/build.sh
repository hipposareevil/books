#!/bin/bash

############
# Build nginx image
# 
############

# Our real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Image we build
project=$(cat $our_directory/webservice.name | xargs)
project_version=$(cat $our_directory/../webservice.version | xargs)

base_image_name="books.${project}"
image_name="${base_image_name}:${project_version}"

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

    # tag as latest
    output=$(docker tag ${image_name} ${base_image_name}:latest)
    tag_result=$?
    if [ $tag_result -eq 0 ]; then
        echo "[[Tagged \"${image_name}\" as \"${base_image_name}:latest\"]]"
    else
        echo "[[Unable to tag image as latest!!!!]]"
    fi


else
    echo ""
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    echo "Unable to build Docker image for $image_name"
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    exit 1
fi
