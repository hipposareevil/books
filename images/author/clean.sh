#!/usr/bin/env bash

#######
# clean project 
#######

# The real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
root=$our_directory/../..

# load in common build file
. $root/buildfiles/build-service.sh

# clean the build
build-service::clean

