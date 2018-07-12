###############
# constants

# the default 'limit'
EXPECTED_DEFAULT_LIMIT=20

# Fonts
bold=$(tput bold)
normal=$(tput sgr0)



########
# Set up variables
#
########
initialize_variables() {
    TOKEN_FILE=auth.token
    rm -f $TOKEN_FILE

    TOKEN_FILE_second=auth.token
    rm -f $TOKEN_FILE_second


    ROOT_URL="http://localhost:8080"
    ADMIN_USER="admin"
    ADMIN_PASSWORD="admin"

    SECOND_USER_PASSWORD="otherpassword"

    # flag to keep running when error is hit
    NO_FAIL=0

    # count of passed tests
    PASSED_TESTS=0
}



######
# Authorize
#
######
authorize_admin() {
    if [ ! -e $TOKEN_FILE ]; then
        echo "[Generate new authentication file]"

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

    echo "[Got authentication, user ID: ${USER_ID}]"
}


#######
# Authorize a second user
#
# takes name & password
#######
authorize_second_user() {
    name=$1
    password=$2

    # create data for POST
read -r -d '' data <<EOF
{
"name": "${name}",
"password":"${password}"
}
EOF

    token=$(curl -s -X POST \
                 ${ROOT_URL}/authorize/token \
                 -H 'cache-control: no-cache' \
                 -H 'content-type: application/json' \
                 -d "${data}"
         )
    echo "$token" > $TOKEN_FILE_second
    

    # get variables
    BEARER_second=$(<$TOKEN_FILE_second jq -r '.token')
    if [ -z "$BEARER_second" ] || [[ "$BEARER_second" == "null" ]]; then
        error "Couldn't get bearer from authentication token for 2nd user."
    fi
    USER_ID_second=$(<$TOKEN_FILE_second jq -r '.userId')
    if [ -z "$USER_ID_second" ] || [[ "$USER_ID_second" == "null" ]]; then
        error "Couldn't get user ID from authentication token for 2nd user."
    fi
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


############
# asserts that the incoming string values are equal
#
# params:
# 1- expected value
# 2- actual value
# 3- error message
#############
assert_string_equals() {
    expected="$1"
    actual="$2"
    message="$3"

    if [[ "$expected" != "$actual" ]]; then
        error "${bold}✗ ${normal} Incorrect '${message}'. Expected: '$expected', got '$actual'."
    else
        echo "✓ Correct '${message}'. Got '$expected'."
        PASSED_TESTS=$(( $PASSED_TESTS + 1))
    fi
}

############
# asserts that the incoming csv contains the desired value
#
# params:
# 1- csv string
# 2- desired value
# 3- what
#############
assert_contains() {
    csv="$1"
    value="$2"
    what="$3"

    if [[ $csv != *"${value}"* ]]; then
        error "${bold}✗ ${normal} '${what}' [$csv] is missing '$value'"
    else
        echo "✓ '${what}' contains '$value' ---> [$csv]"
        PASSED_TESTS=$(( $PASSED_TESTS + 1))
    fi
}



############
# asserts that the incoming number values are equal
#
# params:
# 1- expected value
# 2- actual value
# 3- error message
#############
assert_equals() {
    expected="$1"
    actual="$2"
    message="$3"

    if [ $expected -ne $actual ]; then
        error "${bold}✗ ${normal} Incorrect '${message}'. Expected: '$expected', got '$actual'."
    else
        echo "✓ Correct '${message}'. Got '$expected'."
        PASSED_TESTS=$(( $PASSED_TESTS + 1))
    fi
}

############
# asserts that the incoming number values are NOT equal
#
# params:
# 1- expected value
# 2- actual value
# 3- error message
#############
assert_not_equals() {
    expected="$1"
    actual="$2"
    message="$3"

    if [ $expected -eq $actual ]; then
        error "${bold}✗ ${normal} Incorrect '${message}'. Expected NOT '$expected', got '$actual'."
    else
        echo "✓ Correct '${message}'. Got '$actual', correctly NOT '$expected'"
        PASSED_TESTS=$(( $PASSED_TESTS + 1))
    fi
}


######
# Error out
######
error() {
    >&2 echo ""
    >&2 echo "****************************"
    >&2 echo "$@"
    >&2 echo "****************************"
    if [ ${NO_FAIL} -ne 1 ]; then
        kill -s TERM $TOP_PID
    else
        >&2 echo "NO_FAIL was set, not exiting from error. "
        >&2 echo ""
    fi

}
#######
# Log
######
logit() {
    >&2 echo "[$@]"
}


########
# print incoming data to jq
#######
jqit() {
    echo "$1" | jq -r .
}
