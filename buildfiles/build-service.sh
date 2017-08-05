#!/usr/bin/env bash

####################
# Common buildfile that can build maven or gradle builds.
# It will attempt to use the localhost mvn or gradle, but if they
# are not present this will use docker.
#
# The variable 'our_directory' must be set by each script that
# loads this build script.
####################


#############
# Initalize variables
#############
initialize_variables() {
    # Get name of project
    project=$(cat $our_directory/webservice.name | xargs)

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
        (cd $our_directory; mvn package)
        build_result=$?
    else
        echo "Running maven via docker."
        ###
        # build project via maven using a docker container
        # volumes:
        # our_directory ==> /usr/src/mymaven   (source)
        # .m2          ==> /opt/.m2           (maven repo cache)
        docker run --rm \
               -it \
               -v "$our_directory":/usr/src/mymaven \
               -v "$our_directory"/../../.m2:/opt/.m2 \
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
        (cd $our_directory; gradle build)
        build_result=$?
    else
        echo "Running gradle via docker."
        ###
        # build project via gradle using a docker container
        # volumes:
        # our_directory ==> /work          (source)
        # .gradle      ==> /GRADLE_CACHE  (gradle repo cache)
        docker run --rm \
               -it \
               -v "$our_directory":/work \
               -v "$our_directory"/../../.gradle:/GRADLE_CACHE \
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
    (cd $our_directory; docker build "$our_directory" -t $image_name)
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
    if [ -z "$our_directory" ]; then
        echo "Calling script must set 'our_directory' variable"
        exit 1
    fi

    initialize_variables

    # build project
    echo "Building web service '$project'"
    if [ -e $our_directory/pom.xml ]; then
        build_maven
    elif [ -e $our_directory/build.gradle ]; then
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
