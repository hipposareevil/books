#!/usr/bin/env bash

####
# build all projects and their docker images

# get all build.sh files
#projects=$(find $PWD -maxdepth 2 -mindepth 2 -type f -name "build.sh")
projects=$(ls */build.sh)

# break up into array by space
IFS=$'\n' projectList=(${projects//$' '/ })

# go through all build files
for i in "${!projectList[@]}"
do
    project=$(dirname "${projectList[i]}")
    echo "  Building project '$project'"
    $project/build.sh
    if [ $? -ne 0 ]; then
        echo "Unable to build $project, exiting"
        exit -1
    fi
    echo "  '$project' completed."
done

echo ""
echo "'books' webservice built!" 
