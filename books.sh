#!/usr/bin/env bash

# Script to manage the books application. Supports
# * build
# * start
# * stop
# * clean
# * clean-cache


# See if docker-compose exists. If it doesn't, use the docker-compose-run container.
# taken from https://docs.docker.com/compose/install/#/install-as-a-container


########
# Set up variables
#
########
initialize_variables() {
    dc=docker-compose
    if ! type "$dc" > /dev/null 2>&1 ; then
        echo "Unable to find docker-compose, using script from utils"
        dc=utils/docker-compose-run.sh
    fi
}

############
# Usage
# 
############
usage() {
  echo
  echo "Usage: $0 [build] [start] [stop] [clean]"
  echo ""
  echo "* build       : Builds application"
  echo "* start       : Starts application"
  echo "* stop        : Stops application"
  echo "* clean       : Cleans each application's build"
  echo "* clean-cache : Cleans maven & gradle repositories"
  echo ""
  exit 0;
}



############
# Stop the application
# 
############
stop() {
    $dc down
    echo "Stopped 'books' app"
}

############
# Start the application
# 
############
start() {
    # start up the containers and daemonize docker-compose
    $dc up -d

    echo ""
    echo "Running 'books' on localhost:8080. Try http://localhost:8080/swagger/"
    echo "Note: the app takes a few seconds to warm up while the DB initializes."
}

############
# Clean
# remove the .m2 and .gradle directories
# 
############
clean-cache() {
    echo "Cleaning up maven and gradle repositories"
    rm -rf .m2 .gradle
}


############
# Build all images
# 
############
build() {
    ####
    # build all projects and their docker images

    # build common
    echo " -------------------------------------------------"
    echo ""
    echo "  Building project 'mybooks_common'"
    mybooks_common/build.sh
    if [ $? -ne 0 ]; then
        echo "Unable to build mybooks_common - exiting!"
        exit 1
    fi
    
    # get all build.sh files
    projects=$(ls images/*/build.sh)

    # break up into array by space
    IFS=$'\n' projectList=(${projects//$' '/ })

    # go through all build files
    for i in "${!projectList[@]}"
    do
        project=$(dirname "${projectList[i]}")
        echo ""
        echo " -------------------------------------------------"
        echo ""
        echo "  Building project '$project'"
        $project/build.sh 
        if [ $? -ne 0 ]; then
            echo "Unable to build $project, exiting."
            exit 1
        fi

        echo "  '$project' completed."
    done

    echo ""
    echo "All webservices built!" 
}

###########
# Iterate on all of the projects and call some executable.
#
# params:
# 1- script to call, e.g. 'build.sh'
###########
_iterate_projects() {
    script=$1

    # find all scripts 
    projects=$(ls images/*/$script)

    # break up into array by space
    IFS=$'\n' projectList=(${projects//$' '/ })

    # go through all build files
    for i in "${!projectList[@]}"
    do
        project=$(dirname "${projectList[i]}")
        echo ""
        echo " -------------------------------------------------"
        echo ""
        echo "  Running project '$project'"
        $project/$script
        if [ $? -ne 0 ]; then
            echo "Unable to execute $script for $project, exiting"
            exit 1
        fi

        echo "$script for '$project' completed."
    done
}

##############
# Clean all projects
##############
clean() {
    _iterate_projects "build.sh clean"
}


############
# main
# 
############
main() {
    initialize_variables

    if [ $# -eq 0 ]
    then
        usage
    fi
    arg=$1

    for arg in $@
    do
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
            "start")
                start
                exit 0
                ;;
            "stop")
                stop
                exit 0
                ;;
            "clean-cache")
                clean-cache
                exit 0
                ;;
        esac
    done
}


# Call main
main "$@"
