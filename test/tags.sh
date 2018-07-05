
######################
#
# tag related calls
#
#
######################


##########
# get tag by id
#
##########
get_tag_by_id() {
    tag_id="$1"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET ${ROOT_URL}/tag/${tag_id} \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /tag for tag '$tagName'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting tag '$tag_id'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


##########
# Get all tags
#
##########
get_all_tags() {
    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/tag" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /tag for all tags"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        logit "Error getting all tags: $code: $result"
        error "http code: $code"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}

##########
# Get all tags w/ offset and limit
#
##########
get_all_tags_with_offset_limit() {
    offset=$1
    limit=$2

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/tag?offset=${offset}&limit=${limit}" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /tag for all tags"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        logit "Error getting all tags: $code: $result"
        error "http code: $code"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}




########
# delete all tags
#
########
delete_all_tags() {
    # get 1000 tags
    tags=$(get_all_tags_with_offset_limit 0 1000 )
    ids=$(echo "${tags}" | jq -r ".data[].id" )
    num=$(echo "${tags}" | jq -r ".data | length" )

    echo ""
    echo "Delete all ($num) tags."

    for id in $ids
    do
        $(delete_tag $id)
    done
}

##########
# delete tag
#
# params:
# tag id
##########
delete_tag() {
    tag_id="$1"

#    logit "delete tag $tag_id"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X DELETE ${ROOT_URL}/tag/${tag_id} \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making DELETE to /tag for tag $tag_id"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        echo "Error deleting tag '$tag_id'. code: $code: $error"
    fi

#    logit "got error code from delete: $code"

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
}



##########
# update tag
#
#########
update_tag() {
    tag_id="$1"
    new_name="$2"

read -r -d '' post_data <<EOF
{
"name":"$new_name"
}
EOF


    ##########
    # update in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X PUT "${ROOT_URL}/tag/${tag_id}" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making PUT for test tag"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        error "Error making PUT for test tag. code: $code: $result"
    fi

    tag_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    logit "tag updated"
}


##########
# Create tag in db
# 
##########
tag::create_tag() {
    name="$1"

    read -r -d '' post_data <<EOF
{
"name":"$name"
}
EOF

    ##########
    # create in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X POST "${ROOT_URL}/tag" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making POST for test tag"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        if [[ "$code" == "409" ]]; then
            error "Tag already exists!"
        else
            error "Error making POST for test tag. code: $code: $result"
        fi
    fi

    tag_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
#    logit "tag created"
    echo "$tag_result"
}


######
# print info for tag
# 
######
print_tag_info() {
    tag_info="$1"
    name=$(echo "$tag_info" | jq -r .name)
    id=$(echo "$tag_info" | jq -r .id)

    echo "Tag: '$name', ID: '$id'"
}



##############
# Clean tags
##############
tag::clean() {
    delete_all_tags 

}

##########
# Main tag test
#
#########
tag::main_test() {
    echo "Get all tags"
    all_tags=$(get_all_tags)

    echo ""
    echo "Delete all tags"
    delete_all_tags 
    echo "done deleting tags"

    echo ""
    echo "Create tag: 'sci-fi'"
    tag=$(tag::create_tag "sci-fi")
    tagname=$(echo "$tag" | jq -r .name)
    assert_string_equals "sci-fi" $tagname "Tag's name"

    echo ""
    echo "Create tag: 'e-book'"
    tag=$(tag::create_tag "e-book")
    tag_id=$(echo "$tag" | jq -r .id)
    tagname=$(echo "$tag" | jq -r .name)
    assert_string_equals "e-book" $tagname "Tag's name"

    echo ""
    echo "Get single tag: $tag_id"
    tag=$(get_tag_by_id $tag_id)
    tagname=$(echo "$tag" | jq -r .name)
    assert_string_equals "e-book" $tagname "Tag's name"
    
    echo ""
    echo "Update single tag with new name"
    ignore=$(update_tag $tag_id "ebookkkks")

    echo "Verifying new tag..."
    tag=$(get_tag_by_id $tag_id)
    tagname=$(echo "$tag" | jq -r .name)
    assert_string_equals "ebookkkks" $tagname "Tag's updated name"

    # remove everything
    tag::clean
}


###############
#
# Test the limits and offsets for large datasets
#
###############
tag::test_limit_offset() {
    echo ""
    echo "[[ Tag Limit/Offset test ]]"

    # num tags to create
    COUNT=40

    echo "Creating $COUNT tags"

    idx=1
    while [ $idx -le $COUNT ]
    do
        idx=$(( $idx + 1 ))
        tagname="tag_${idx}"
        result=$(tag::create_tag $tagname)
    done

    ############
    # Default returns
    # get tags and see how many
    all_tags=$(get_all_tags)
    total=$(echo "$all_tags" | jq -r .total)
    offset=$(echo "$all_tags" | jq -r .offset)
    limit=$(echo "$all_tags" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in tags returned"
    assert_equals $EXPECTED_DEFAULT_LIMIT $limit "limit number tags"
    assert_equals $COUNT $total "total number tags"

    ############
    # new limit
    echo ""
    echo "Testing new limit"
    all_tags=$(get_all_tags_with_offset_limit 0 500)
    total=$(echo "$all_tags" | jq -r .total)
    offset=$(echo "$all_tags" | jq -r .offset)
    limit=$(echo "$all_tags" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in tags returned"
    assert_equals $COUNT $limit "limit number tags"
    assert_equals $COUNT $total "total number tags"


    ############
    # new offset
    echo ""
    echo "Testing new offset"
    all_tags=$(get_all_tags_with_offset_limit 10 10)
    total=$(echo "$all_tags" | jq -r .total)
    offset=$(echo "$all_tags" | jq -r .offset)
    limit=$(echo "$all_tags" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 10 ${offset} "offset in tags returned"
    assert_equals 10 $limit "limit number tags"
    assert_equals $COUNT $total "total number tags"

    # remove everything
    tag::clean

    echo ""
    echo "[[ DONE Tag Limit/Offset test ]]"
}


###############
#
# Test tag endpoint
#
###############
test_tag() {
    echo ""
    echo "[ Tag test ]"

    tag::main_test

    tag::test_limit_offset

    echo "[ DoneTag test ]"
}
