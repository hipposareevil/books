#!/usr/bin/env bash

# build image

# image we build
imageName="books.query:latest"

# our real directory (so this can be called from outside directories)
ourDirectory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

###
# create image for 'query'
docker build "$ourDirectory" -t $imageName
if [ $? -eq 0 ]; then
    echo ""
    echo "Built $imageName"
else
    echo ""
    echo "Unable to build project for $iamgeName"
    exit 1
fi
