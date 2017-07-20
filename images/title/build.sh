#!/usr/bin/env bash

#######
# Build project 
#######

# The real directory (so this can be called from outside directories)
ourDirectory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
root=$ourDirectory/../..

# load in common build file
. $root/buildfiles/build-service.sh

# build the project including the image
build

