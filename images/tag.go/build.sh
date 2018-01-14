#!/bin/bash

##############
# SYNOPSIS
#     build.sh
#
# DESCRIPTION
#    Builds the go version of the 'tag' service and image
##############


##############
# initialize_variables
# 
##############
initialize_variables() {
    IMAGE_NAME="books.tag.go"

    ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
}

##########
# Usage statement
#
##########
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Builds the go version of the 'tag' service and Docker image."
    echo ""
    echo "Options:"
    echo "  -h               Print this message."
    echo "  -c/--clean       Cleans the project."
    echo ""

n    exit 0
}


##########
# Clean project
#
##########
clean() {
    echo "[Removing dep vendor, metadata, and pkg directories]"
    rm -f ${ROOT_DIR}/src/github.com/hipposareevil/Gopkg*
    rm -rf ${ROOT_DIR}/src/github.com/hipposareevil/vendor
    rm -rf ${ROOT_DIR}/pkg
}


##############
# Run 'dep' for dependencies
##############
run_dep() {
    # Use 'dep' (https://github.com/golang/dep) to manage the  packages for this project.
    # This is done via the 'hipposareevil/alpine-dep' image
    # All packages are downloaded into the src/github.com/hipposareevil/vendor directory
    echo "[Running go's 'dep' against source]"
    docker run -it -e GOPATH=/go -v ${ROOT_DIR}:/go -w /go hipposareevil/alpine-dep init src/github.com/hipposareevil
    echo "[Done grabbing dependencies]"
    echo ""
}

##############
# Build image
##############
build_image() {
    echo "[Building docker image]"

    # This will run 'go build' in the image and then copy
    # the binary to the appropriate end location.
    docker build -t ${IMAGE_NAME} .
    
    echo "[Done with build for ${IMAGE_NAME}]"
}

##############
# Main
#
# options: -h -v
##############
main() {
    # Initialize
    initialize_variables

    # parse params
    for arg in $@
    do
        case $arg in
            "-h"|"--help")
	        usage
	        exit 0
	        ;;
            "-c"|"--clean")
                clean
                exit 0
                ;;
        esac
    done

    # get dependencies
    run_dep

    # Build docker image
    build_image
}

# Run main
main "$@"