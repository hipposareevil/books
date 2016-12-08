#!/usr/bin/env bash

# Script to manage the books application. Supports
# * build
# * start
# * stop
# * clean


# See if docker-compose exists. If it doesn't, use the docker-compose-run container.
# taken from https://docs.docker.com/compose/install/#/install-as-a-container

dc=docker-compose
if ! type "$dc" > /dev/null 2>&1 ; then
    dc=utils/docker-compose-run.sh
fi


# Stop the application
stop() {
    $dc down
    echo "Stopped 'books' app"
}

# Start the application
start() {
    # start up the containers and daemonize docker-compose
    $dc up -d

    echo ""
    echo "Running 'books' on localhost:8080. Try http://localhost:8080/swagger/"
    echo "Note: the app takes a few seconds to warm up while the DB initializes."
}

# Clean
# remove the .m2 and .gradle directories
clean() {
    echo "Cleaning up maven and gradle repositories"
    rm -rf .m2 .gradle
}

# Build
build() {
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
        echo ""
        echo " -------------------------------------------------"
        echo ""
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
}

# Usage
usage()
{
  echo
  echo "Usage: $0 [build] [start] [stop] [clean]"
  echo ""
  echo "* build  : Builds application"
  echo "* start  : Starts application"
  echo "* stop   : Stops application"
  echo "* clean  : Cleans maven & gradle repositories"
  echo ""
  exit 0;
}



######################
# main

if [ $# -eq 0 ]
then
 usage
fi
arg=$1

for arg in $@
do
  case $arg in
      "-help"|"--help")
	  usage
	  exit 0
	  ;;
      "build")
          build
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
      "clean")
          clean
          exit 0
          ;;
  esac
done
