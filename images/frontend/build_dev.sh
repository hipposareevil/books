#!/usr/bin/env bash

# our real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# image we build
project=$(cat $our_directory/webservice.name | xargs)
project_version=$(cat $our_directory/../webservice.version | xargs)

image_name="books.${project}:${project_version}-dev"

# set docker image labels
BUILD_TIME=$(date +%Y-%m-%dT%H:%M:%S%Z)
VERSION_TAG="${project_version}-dev"

docker build \
       "$our_directory" \
       --build-arg BUILD_TIME=${BUILD_TIME} \
       --build-arg VERSION=${VERSION_TAG} \
       -t $image_name

echo ""
echo "Built $image_name"
