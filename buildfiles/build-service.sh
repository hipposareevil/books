#!/bin/bash

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
    if [ -z "$our_directory" ]; then
        echo "NOTE: Calling script must set 'our_directory' variable."
        exit 1
    fi

    # Get name of project
    project=$(cat $our_directory/webservice.name | xargs)

    image_name="books.$project:latest"
}

#########
# Usage
#
#########
usage() {
    echo
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Builds image '$image_name'"
    echo ""
    echo "Options:"
    echo "  -h,--help   : Print this message."
    echo "  build       : Builds the application"
    echo "  clean       : Cleans the application"
    echo ""
    exit 0;

}


#########
# Run a maven command
#
# params:
# 1- command to run
#########
_maven() {
    command=$@

  # See if mvn is already installed
    which mvn > /dev/null
    if [ $? -eq 0 ]; then
        echo "[[Using local maven]]"
        (cd $our_directory; mvn $command)
    else
        echo "[[Running maven via docker]]"
        ###
        # build project via maven using a docker container
        # volumes:
        # our_directory ==> /usr/src/mymaven   (source)
        # .m2          ==> /opt/.m2           (maven repo cache)
        docker run --rm \
               -it \
               -v "$our_directory":/usr/src/mymaven \
               -v "${our_directory}/lib":/tmp/lib \
               -v "$our_directory"/../../.m2:/opt/.m2 \
               -w /usr/src/mymaven \
               maven:3.3.9-jdk-8-alpine \
               mvn \
               -Dmaven.repo.local=/opt/.m2/ \
               $command
        build_result=$?
    fi
    # return result of build
    return $build_result
}

#############
# Build maven
#
#############
build_maven() {
    _maven "package"
    return $?
}

#############
# Copy common jars into a local lib directory to be
# accessible for a docker version of maven or gradle
#
#############
copy_common_jars() {
    rm -rf /tmp/lib
    mkdir -p /tmp/lib
    mkdir -p ${our_directory}/lib
    cp ${our_directory}/../../mybooks_common/repos/*.jar ${our_directory}/lib/
    cp ${our_directory}/../../mybooks_common/repos/*.jar /tmp/lib
}


#############
# Load our mybooks_common
#
#############
load_common_jars_for_maven() {
    echo "Loading mybooks-common into project"
#    _maven install:install-file -Dfile=${our_directory}/lib/mybooks_common-1.0.jar -DgroupId=com.wpff.common -DartifactId=mybooks-common -Dversion=1.0 -Dpackaging=jar
    _maven install:install-file -Dfile=/tmp/lib/mybooks_common-1.0.jar -DgroupId=com.wpff.common -DartifactId=mybooks-common -Dversion=1.0 -Dpackaging=jar
    if [ $? -ne 0 ]; then
        echo "Unable to load mybooks_common.jar into project '$project'"
        exit 1
    fi       
}


#############
# Clean maven
#
#############
clean_maven() {
    _maven "clean"
    return $?
}


############
# Run a gradle command
#
# params:
# 1- command to run
############
_gradle() {
    command=$1

    which gradle > /dev/null
    if [ $? -eq 0 ]; then
        echo "[[Using local gradle]]"
        (cd $our_directory; gradle "$command")
        build_result=$?
    else
        echo "[[Running gradle via docker]]"
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
               ""$command""
        build_result=$?
    fi
    # return result of command
    return $build_result
}

#############
# Build gradle
#
#############
build_gradle() {
    _gradle "build"
    return $?
}

#############
# Clean gradle
#
#############
clean_gradle() {
    _gradle "clean"
    return $?
}

#############
# Build image
#
#############
build_image() {
    local then=$(date +%s)
    echo "[[Building Docker image '$image_name']]"

    # set build time
    BUILD_TIME=$(date +%Y-%m-%dT%H:%M:%S%Z)
    VERSION_TAG="latest"

    (cd $our_directory;
     docker build -t ${image_name} \
            --build-arg BUILD_TIME=${BUILD_TIME} \
            --build-arg VERSION=${VERSION_TAG} \
            "$our_directory" )

    build_result=$?

    local now=$(date +%s)
    local elapsed=$(expr $now - $then)

    if [ $build_result -eq 0 ]; then
        echo ""
        echo "[[Built $image_name in $elapsed seconds]]"
    else
        echo ""
        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        echo "Unable to build Docker image for $image_name"
        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        exit 1
    fi
}


#############
# Build the project
#
#############
build() {
    # copy in common
    copy_common_jars

    # build project
    echo "[[Building web service '$project']]"
    local then=$(date +%s)

    if [ -e $our_directory/pom.xml ]; then
        load_common_jars_for_maven
        build_maven
    elif [ -e $our_directory/build.gradle ]; then
        build_gradle
    else 
        echo "[[Unable to find maven or gradle artifacts to build.]]"
        echo "[[Exiting]]"
        echo 1
    fi

    build_success=$?
    if [ $build_success -ne 0 ]; then
        echo "[[Unable to build successfully.]]"
        echo "[[Exiting]]"
        exit 1
    fi

    local now=$(date +%s)
    local elapsed=$(expr $now - $then)
    echo "[[Built application in $elapsed seconds]]"

    if [ $? -ne 0 ]; then
        echo "[[Unable to build project. Exiting.]]"
        exit 1
    fi

    # build image
    build_image

    #done
    date=$(date)
    echo "[[Build for '$project' complete on $date]]"
}


#############
# Clean the project
#
#############
clean() {
    # build project
    echo "[[Cleaning web service '$project']]"
    if [ -e $our_directory/pom.xml ]; then
        clean_maven
    elif [ -e $our_directory/build.gradle ]; then
        clean_gradle
    else 
        echo "Unable to find maven or gradle artifacts to perform the clean."
        echo "Exiting"
        echo 1
    fi

    if [ $? -ne 0 ]; then
        echo "Unable to clean the project. Exiting"
        exit 1
    fi

    #done
    echo "[[Clean for '$project' complete]]"
}

############
# main
# 
############
main() {
    # init
    initialize_variables

    # default to "build"
    if [ $# -eq 0 ]
    then
        arg="build"
    else
        arg=$1
    fi

    # check arg
    case $arg in
        "-h"|"--help")
	    usage
	    exit 0
	    ;;
        "build")
            build
            exit 0
            ;;
        "clean")
            clean
            exit 0
            ;;
        \?) #unknown
            usage
            ;;
    esac
}


# Call main
main "$@"
