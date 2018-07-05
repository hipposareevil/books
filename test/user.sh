
######################
#
# user related calls
#
#
######################


##########
# get user by id
#
##########
get_user_by_id() {
    user_id="$1"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET ${ROOT_URL}/user/${user_id} \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /user for user '$userName'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting user '$userName'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


##########
# Get all users
#
##########
get_all_users() {
    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/user" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /user for all users"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting all users: $code: $error. http_code: $result."
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


###############
# Get all users w/ offset & limit
# 
#
###############
get_all_users_with_offset_limit() {
    offset=$1
    limit=$2

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/user?offset=${offset}&limit=${limit}" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /user for all users"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting all users: $code: $error. http_code: $result."
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}
     



##########
# delete user
#
# params:
# user id
##########
delete_user() {
    user_id="$1"

    if [ $user_id -eq 1 ]; then
        logit "Not deleting user 1 as that's the admin user."
        return
    fi

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X DELETE ${ROOT_URL}/user/${user_id} \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making DELETE to /user for user $user_id"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        echo "Error deleting user '$user_id'. code: $code: $error"
    fi

#    logit "got error code from delete: $code"

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
}



##########
# update user
#
#########
_update_user() {
    user_id="$1"
    post_data="$2"

    ##########
    # create in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X PUT "${ROOT_URL}/user/${user_id}" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making PUT for test user"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        error "Error making PUT for test user. code: $code: $result"
    fi

    user_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    logit "user updated"
}

##########
# Update user
##########
update_user() {
    user_id=$1

read -r -d '' user_data <<EOF
{
"data" : "new data"
}
EOF

    _update_user $user_id "$user_data"
}


##########
# Create user in db
# 
##########
_create_user() {
    post_data="$1"

    ##########
    # create in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X POST "${ROOT_URL}/user" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making POST for test user"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        if [[ "$code" == "409" ]]; then
            error "User already exists!"
        else
            error "Error making POST for test user. code: $code: $result"
        fi
    fi

    user_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
#    logit "user created"
    echo "$user_result"
}


#########
# Create user
#
# param:
# 1- name of user
#########
user::create_user() {
    name="$1"
read -r -d '' user_data <<EOF
{
"name":"${name}",
"userGroup":"othergroup",
"data":"n/a",
"password":"${SECOND_USER_PASSWORD}"
}
EOF

   # create user
   _create_user "${user_data}"
}

######
# print info for user
# 
######
print_user_info() {
    user_info="$1"
    name=$(echo "$user_info" | jq -r .name)
    id=$(echo "$user_info" | jq -r .id)
    group=$(echo "$user_info" | jq -r .userGroup)

    echo "User: '$name', ID: '$id', Group: '$group'"
}

###############
# clean users, besides the admin user
#
###############
user::clean() {
    # offset of 1 to skip the admin user
    users=$(get_all_users_with_offset_limit 1 1000)
    ids=$(echo "${users}" | jq -r ".data[].id" )
    num=$(echo "${users}" | jq -r ".data | length" )

    echo ""
    echo "Delete all ($num) users."

    for id in $ids
    do
        $(delete_user $id)
    done
}



###############
#
# main test for user
#
###############
user::main_test() {
    echo "Get all users"
    all_users=$(get_all_users)

    echo ""
    echo "Create user: 'fooz'"
    user=$(user::create_user "fooz")
    user_id=$(echo "$user" | jq -r .id)
    print_user_info "$user"

    echo ""
    echo "Get single user"
    user=$(get_user_by_id $user_id)
    username=$(echo "$user" | jq -r .name)
    assert_string_equals "fooz" $username "Username"

    echo ""
    echo "Update single user"
    user=$(update_user $user_id)

    echo ""
    echo "Verifying updated user..."
    user=$(get_user_by_id $user_id)
    userdata=$(echo "$user" | jq -r .data)
    assert_string_equals "new data" "$userdata" "Updated users data"

    user::clean
}


###############
#
# Test the limits and offsets for large datasets
#
###############
user::test_limit_offset() {
    echo ""
    echo "[[ User Limit/Offset test]]"

    # num users to create
    COUNT=40

    echo "Creating $COUNT users"

    idx=1
    while [ $idx -le $COUNT ]
    do
        idx=$(( $idx + 1 ))
        username="user_${idx}"
        result=$(user::create_user $username)
    done

    COUNT=$(( $COUNT +1 ))

    #######
    # Default returns
    # get users and see how many
    echo ""
    echo "Testing default limit (20)"

    all_users=$(get_all_users)
    total=$(echo "$all_users" | jq -r .total)
    offset=$(echo "$all_users" | jq -r .offset)
    limit=$(echo "$all_users" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in users returned"
    assert_equals $EXPECTED_DEFAULT_LIMIT $limit "limit number users"
    assert_equals $COUNT $total "total number users"

    #######
    # new limit
    echo ""
    echo "Testing new limit"
    all_users=$(get_all_users_with_offset_limit 0 500)
    total=$(echo "$all_users" | jq -r .total)
    offset=$(echo "$all_users" | jq -r .offset)
    limit=$(echo "$all_users" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in users returned"
    assert_equals $COUNT $limit "limit number users"
    assert_equals $COUNT $total "total number users"


    #######
    # new offset
    echo ""
    echo "Testing new offset"
    all_users=$(get_all_users_with_offset_limit 10 10)
    total=$(echo "$all_users" | jq -r .total)
    offset=$(echo "$all_users" | jq -r .offset)
    limit=$(echo "$all_users" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 10 ${offset} "offset in users returned"
    assert_equals 10 $limit "limit number users"
    assert_equals $COUNT $total "total number users"

    #######
    # new offset
    echo ""
    echo "Testing 2nd new offset"
    all_users=$(get_all_users_with_offset_limit 13 2)
    total=$(echo "$all_users" | jq -r .total)
    offset=$(echo "$all_users" | jq -r .offset)
    limit=$(echo "$all_users" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 13 ${offset} "offset in users returned"
    assert_equals 2 $limit "limit number users"
    assert_equals $COUNT $total "total number users"


    user::clean
    echo "[[DONE User Limit/Offset test]]"
}



###############
#
# Test user endpoint
#
###############
test_user() {
    echo ""
    echo "[ User test ]"

    user::main_test

    user::test_limit_offset    

    echo "[ Done User test ]"
}
