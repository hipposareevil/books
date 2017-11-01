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
#


# our real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# image we build
imageName="books.frontend"

## 1
# build dev first
echo "Building the books.frontend:dev version first"
$our_directory/buildDev.sh

## 2
# Run npm run build in container
echo "Now building $imageName"
docker run -it -v $our_directory/content/mybooks:/scratch ${imageName}:dev npm install run build

# Get docker to skip the cache:
thedate=$(date)
echo "$thedate" > ${our_directory}/content/mybooks/dist/build.time

## 3
docker build -f ${our_directory}/Dockerfile.prod -t ${imageName}:prod $our_directory

echo ""
echo "Built $imageName"
