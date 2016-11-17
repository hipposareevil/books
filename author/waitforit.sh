#!/bin/bash

# curl --connect-timeout 0.5 -sL localhost:888 -o /dev/null

# This program polls the incoming URL for a connection.
# When URL is available this script will call the incoming command.
# If URL is not available before the timeout, will exit with 1 error code.
#
# params:
# 1- url
# 2- timeout
# 3- command to run
#
# will run command if successful connection, 1 otherwise


################
# process params
if [ $# -lt 3 ]; then
    echo "usage: $0 <URL> <timeout in seconds>. e.g. $0 localhost:8888 3"
    exit 1
fi

url=$1
timeout=$2
shift
shift
command="$@"


###############
# local vars
elapsedTime=0
# how long to wait on a curl connection
waitTime=1
# could we connect
connected=0


while [[ $elapsedTime -lt $timeout  && $connected -eq 0 ]]
do
    resultString=$(curl -X GET --connect-timeout $waitTime -sL $url -o /dev/null)
    result=$?
    if [ $result -eq 0 ]; then
	# connection is ok.
	echo "$url is up."
        connected=1
    else
        # not up yet, sleep for a bit
        sleep $waitTime
        echo -n "."
        elapsedTime=$(($elapsedTime + $waitTime))
    fi
done

if [ $connected -eq 1 ]; then
    exec $command
else    
    # unable to connect
    echo "Unable to connect to '$url' in $timeout seconds"
    exit 1
fi


