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

url_to_check=$1
timeout=$2
shift
shift
command="$@"


###############
# local vars
elapsedTime=0
# how long to wait on a curl connection
waitTime=1


# could we connect. 
# -1 mean valid connection but bad URL
# 0 means no connection
# 1 means valid connection and URL
connected=0

while [[ $elapsedTime -lt $timeout  && $connected -eq 0 ]]
do

    # If url_to_check starts with http, then verify the full URL is accessible,
    # otherwise, just do a GET
    if [[ $url_to_check == http* ]]; then
        # URL is HTTP
        resultString=$(curl  -s -I -w %{http_code} --connect-timeout $waitTime -sL $url_to_check -o /dev/null)
        result=$?

        # verify connection and result
        if [ $result -eq 0 ]; then
            # connected to server. Let's verify the resource exists
            if [ "$resultString" = "200" ] ; then
                connected=1
            else
                connected=-1
            fi
        fi
    else
        # URL is non HTTP, do GET
        resultString=$(curl -X GET --connect-timeout $waitTime -sL $url_to_check -o /dev/null)
        result=$?

        # verify connection and result
        if [ $result -eq 0 ]; then
            connected=1
        fi
    fi

    if [ $connected -eq 0 ]; then
        # No connection to server, sleep for a bit
        sleep $waitTime
        elapsedTime=$(($elapsedTime + $waitTime))
    fi
done

if [ $connected -eq 1 ]; then
    # connect was valid
    echo "$url_to_check is up."
    exec $command
elif [ $connected -eq -1 ]; then
    # able to connect, but bad HTTP code
    echo "Able to connect to $url_to_check, but got HTTP code $resultString"
    exit 1
elif [ $connected -eq 0 ]; then
    # unable to connect to server
    echo "Unable to connect to '$url_to_check' in $timeout seconds. Exiting."
    exit 1
fi


