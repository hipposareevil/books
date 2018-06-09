#!/bin/bash

####################
# Common buildfile that can build maven or gradle builds.
# It will attempt to use the localhost mvn or gradle, but if they
# are not present this will use docker.
#
# The variable 'our_directory' must be set by each script that
# loads this build script.
####################

# Load in sub build files
ROOT_DIRECTORY="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${ROOT_DIRECTORY}/build_java.sh
. ${ROOT_DIRECTORY}/build_golang.sh


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
    if [ -e ${our_directory}/src/github.com/hipposareevil ]; then
        echo "  dep         : Download dependencies"
    fi
    echo ""

    # add extra info for golang builds
    if [ -e ${our_directory}/src/github.com/hipposareevil ]; then
        tmp=$(echo "${project}" | awk -F. '{print $1}')
        echo "To manually build the '${project}' service in golang:"
        echo "$ export GOPATH=`pwd`"
        echo "$ go build github.com/hipposareevil/${tmp}"
        echo ""
    fi


    exit 0;

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

    # run docker build
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
    echo "[[Building project '$project']]"

    ## Build project
    # Determine build type:
    # golang, maven, gradle
    if [ -e ${our_directory}/src/github.com/hipposareevil ]; then
        golang::build
    else
        java::build
    fi

    ## Build image
    build_image

    date=$(date)
    echo "[[Build for '$project' complete on $date]]"
}


#############
# Clean the project
#
#############
clean() {
    echo "[[Cleaning project '$project']]"

    # determine build type:
    # golang, maven, gradle
    if [ -e ${our_directory}/src/github.com/hipposareevil ]; then
        golang::clean
    else
        java::clean
    fi

    echo "[[Clean for '$project' complete]]"
}


#############
# Get dependencies
#
#############
dependencies() {
    echo "[[Getting dependencies project '$project']]"

    # determine build type:
    # golang, maven, gradle
    if [ -e ${our_directory}/src/github.com/hipposareevil ]; then
        golang::clean
        golang::run_dep
    else
        echo "[[no-op for java]]"
    fi

    echo "[[Dependencies for '$project' complete]]"
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
        "buildimage")
            build_image
            exit 0
            ;;
        "clean")
            clean
            exit 0
            ;;
        "dep")
            dependencies
            exit 0
            ;;
        \?) #unknown
            usage
            ;;
    esac
}


# Call main
main "$@"
