#!/usr/bin/env bash

# Build title project via maven
# then build docker image

# image we build
imageName="books.title:latest"

# our real directory (so this can be called from outside directories)
ourDirectory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

###
# build project via maven using a docker container
# volumes:
# ourDirectory ==> /usr/src/mymaven   (source)
# .m2          ==> /opt/.m2           (maven repo cache)
docker run --rm \
       -it \
       -v "$ourDirectory":/usr/src/mymaven \
       -v "$ourDirectory"/../../.m2:/opt/.m2 \
       -w /usr/src/mymaven \
       maven:3.3.9-jdk-8-alpine \
       mvn \
       -Dmaven.repo.local=/opt/.m2/ \
       package


if [ $? -eq 0 ]; then
    ###
    # create image for 'title'
    docker build "$ourDirectory" -t $imageName

    echo ""
    echo "Built $imageName"
else
    echo ""
    echo "Unable to build project for $imageName"
    exit 1
fi
