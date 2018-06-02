#!/bin/bash

####################3
#
# Build the dev and production versions of frontend.
#
# 1- Create dev version.
#  This relies on the content/mybooks directory being mounted into /scratch in the container
# 2- Run npm in a container that is mounted to content/mybooks
#  This will install everything for NPM and run a build, which results in the content/mybooks/dist directory
# 3- Create prod version
#  This copies the content/mybooks/dist directory into Nginx's html dir.


# our real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# image we build
image_name="books.frontend"

# keep track of time
then=$(date +%s)
echo "[[Building Docker image '$image_name']]"

## 1
# build dev first
echo "Building the books.frontend:dev version first"
$our_directory/buildDev.sh

## 2
# Run npm run build in container
echo "Now building $image_name"
# make directory that we own so it's not owned by root
mkdir -p $our_directory/content/mybooks/dist
docker run --rm -it -v $our_directory/content/mybooks:/scratch ${image_name}:dev npm install 
docker run --rm -it -v $our_directory/content/mybooks:/scratch ${image_name}:dev npm run build

# Get docker to skip the cache:
thedate=$(date)
echo "$thedate" > ${our_directory}/content/mybooks/dist/build.time

## 3
# set build time
BUILD_TIME=$(date +%Y-%m-%dT%H:%M:%S%Z)
VERSION_TAG="prod"

docker build \
       -f ${our_directory}/Dockerfile.prod \
       -t ${image_name}:prod \
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
