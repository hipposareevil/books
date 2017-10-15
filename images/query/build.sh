#!/usr/bin/env bash

#######
# Build project 
#######

# The real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
root=$our_directory/../..

# copy in common
mkdir -p ${our_directory}/lib
cp ${our_directory}/../../mybooks_common/repos/*.jar ${our_directory}/lib/

# load in common build file
. $root/buildfiles/build-service.sh

# build the project including the image
build-service::build

