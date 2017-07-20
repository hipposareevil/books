#!/usr/bin/env bash

####################
# Common buildfile that can build maven or gradle builds.
# It will attempt to use the localhost mvn or gradle, but if they
# are not present this will use docker.
# 
####################


#############
# Initalize variables
#############
initialize_variables() {
    # Get name of project
    project=$(cat $ourDirectory/webservice.name | xargs)

    image_name="books.$project:latest"
}


#############
# Build maven
#
#############
build_maven() {
    # See if mvn is already installed
    which mvn > /dev/null
    if [ $? -eq 0 ]; then
        echo "Using local maven."
        (cd $ourDirectory; mvn package)
        build_result=$?
    else
        echo "Running maven via docker."
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
        build_result=$?
    fi
    # return result of build
    return $build_result
}

#############
# Build gradle
#
#############
build_gradle() {
    which gradle > /dev/null
    if [ $? -eq 0 ]; then
        echo "Using local gradle."
        (cd $ourDirectory; gradle build)
        build_result=$?
    else
        echo "Running gradle via docker."
        ###
        # build project via gradle using a docker container
        # volumes:
        # ourDirectory ==> /work          (source)
        # .gradle      ==> /GRADLE_CACHE  (gradle repo cache)
        docker run --rm \
               -it \
               -v "$ourDirectory":/work \
               -v "$ourDirectory"/../../.gradle:/GRADLE_CACHE \
               hipposareevil/alpine-gradle \
               build
        build_result=$?
    fi
    # return result of build
    return $build_result
}

#############
# Build image
#
#############
build_image() {
    echo "Building Docker image '$image_name'"
    (cd $ourDirectory; docker build "$ourDirectory" -t $image_name)
    build_result=$?

    if [ $build_result -eq 0 ]; then
        echo ""
        echo "Built $image_name"
    else
        echo ""
        echo "Unable to build Docker image for $image_name"
        exit 1
    fi
}


#############
# Build the project
#
#############
build() {
    initialize_variables

    # build project
    echo "Building web service '$project'"
    if [ -e $ourDirectory/pom.xml ]; then
        build_maven
    elif [ -e $ourDirectory/build.gradle ]; then
        build_gradle
    else 
        echo "Unable to find maven or gradle artifacts to build."
        echo "Exiting"
        echo 1
    fi

    if [ $? -ne 0 ]; then
        echo "Unable to build project. Exiting"
        exit 1
    fi

    # build image
    build_image

    #done
    echo "Build for '$project' complete"
}
