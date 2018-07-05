#!/bin/bash

##################
#
# Test for multiple calls for time
#
#
#################

root_dir="$(cd -P "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

trap "exit 1" TERM
export TOP_PID=$$


. ${root_dir}/author.sh
. ${root_dir}/user.sh
. ${root_dir}/util.sh
. ${root_dir}/user_book.sh
. ${root_dir}/tags.sh
. ${root_dir}/book.sh



########
# Set up variables
#
########
initialize_variables() {
    TOKEN_FILE=auth.token

    rm -f $TOKEN_FILE

    ROOT_URL="http://localhost:8080"
    ADMIN_USER="admin"
    ADMIN_PASSWORD="admin"
}

######
# Error out
######
######
error() {
    >&2 echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    >&2 echo "$@"
    >&2 echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    kill -s TERM $TOP_PID
}

logit() {
    >&2 echo "[$@]"
}


##########
# URL encode a string
#
##########
url_encode() {
    what=$1
    result=$(python ./encode.py "$what")
    echo "$result"
}

######
# Authorize
#
######
authorize() {
    if [ ! -e $TOKEN_FILE ]; then
read -r -d '' data <<EOF
{
"name": "${ADMIN_USER}",
"password":"${ADMIN_PASSWORD}"
}
EOF

        token=$(curl -s -X POST \
                     ${ROOT_URL}/authorize/token \
                     -H 'cache-control: no-cache' \
                     -H 'content-type: application/json' \
                     -d "${data}"
             )

        echo "$token" > $TOKEN_FILE
    fi


    # get variables
    BEARER=$(<$TOKEN_FILE jq -r '.token')
    if [ -z "$BEARER" ] || [[ "$BEARER" == "null" ]]; then
        error "Couldn't get bearer from authentication token."
    fi
    USER_ID=$(<$TOKEN_FILE jq -r '.userId')
    if [ -z "$USER_ID" ] || [[ "$USER_ID" == "null" ]]; then
        error "Couldn't get user ID from authentication token."
    fi
}

# Test book query
test_books() {
    all_books=$(get_all_books_with_offset_limit 0 1000)
    numBooks=$(echo $all_books | jq -r '.data | length')
    echo "Num books: $numBooks"
}

# User books
test_user_books() {
    USER_ID=2
    all_books=$(get_all_user_books_with_offset_limit 0 1000)
    numBooks=$(echo $all_books | jq -r '.data | length')
    echo "Num User books: $numBooks"
}

# Test author query
test_authors() {
    all_authors=$(get_all_authors_with_offset_limit 0 1000)
    num=$(echo $all_authors | jq -r '.data | length')
    echo "Num Authors: $num"
}


#################
# Main function.
# 
#################
main() {
    # Initialize
    initialize_variables

    # authorize
    authorize

    for i in `seq 1 3`;
    do
#        test_books
        test_user_books
    done    
    
}

# main
main "$@"
