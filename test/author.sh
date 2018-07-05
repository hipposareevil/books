
######################
#
# author related calls
#
#
######################



##########
# get author by id
#
##########
get_author_by_id() {
    author_id="$1"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET ${ROOT_URL}/author/${author_id} \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /author for author '$authorName'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting author '$authorName'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


##########
# Get all authors
#
##########
get_all_authors() {
    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET ${ROOT_URL}/author \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /author for all authors"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting all authors: $code: $error. http_code: $result."
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}

##########
# Get all authors  w/ offset and limit
#
##########
get_all_authors_with_offset_limit() {
    offset=$1
    limit=$2

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/author?offset=${offset}&limit=${limit}"\
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /author for all authors"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting all authors: $code: $error. http_code: $result."
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}

##########
# get author by name
#
# also takes offset and limit
##########
get_author_by_name() {
    authorName="$1"
    offset="$2"
    limit="$3"

    query=$(url_encode "$authorName")

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/author?name=$query&offset=${offset}&limit=${limit}" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /author for author '$authorName'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting author '$authorName'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}



##########
# delete author
#
# params:
# author id
##########
delete_author() {
    author_id="$1"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X DELETE ${ROOT_URL}/author/${author_id} \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making DELETE to /author for author $author_id"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        echo "Error deleting author '$author_id'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
}



##########
# update author
#
#########
_update_author() {
    author_id="$1"
    post_data="$2"

logit "Author id: $author_id"

    ##########
    # create in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X PUT "${ROOT_URL}/author/${author_id}" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making PUT for test author"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        error "Error making PUT for test author. code: $code: $result"
    fi

    author_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    logit "author updated"
}

##########
# Update author
##########
update_author() {
    author_id=$1

read -r -d '' author_data <<EOF
{
"name":"Isaac Asimov!",
"olKey":"OL34221AXX",
"birthDate":null,
"imageSmall":"https://covers.openlibrary.org/a/olid/OL34221A-L.jpg",
"imageLarge":"https://covers.openlibrary.org/a/olid/OL34221A-L.jpg"
}
EOF

    _update_author $author_id "$author_data"
}


##########
# Create author in db
# 
##########
_create_author() {
    post_data="$1"

    ##########
    # create in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X POST "${ROOT_URL}/author" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making POST for test author"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        if [[ "$code" == "409" ]]; then
            error "Author already exists!"
        else
            error "Error making POST for test author. code: $code: $result"
        fi
    fi

    author_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$author_result"
}


#########
# Create Madeleine Lengle
#
#########
create_author_lengle() {
read -r -d '' author_data <<EOF
{
"name":"Madeleine LEngle",
"olKey":"OL28188A",
"subjects":["Fiction","In library","Juvenile fiction","Madeleine LEngle","Science fiction","Biography","Fantasy","Adventure and adventurers"],
"birthDate":null,
"imageSmall":"https://covers.openlibrary.org/a/olid/OL28188A-S.jpg",
"imageMedium":"https://covers.openlibrary.org/a/olid/OL28188A-M.jpg",
"imageLarge":"https://covers.openlibrary.org/a/olid/OL28188A-L.jpg"
}
EOF

   # create author
   _create_author "${author_data}"
}

#########
# Create Asimov
#
#########
create_author_asimov() {
read -r -d '' author_data <<EOF
{
"name":"Isaac Asimov",
"olKey":"OL34221A",
"birthDate": null,
"imageSmall": null,
"goodreadsUrl": "https://www.goodreads.com/author/show/16667.Isaac_Asimov",
"imageMedium":"https://covers.openlibrary.org/a/olid/OL34221A-M.jpg",
"imageLarge": null
}
EOF

   # create author
   _create_author "${author_data}"
}


#########
# Create generic author
#
# params:
# author name
#########
create_author() {
    name="$1"
read -r -d '' author_data <<EOF
{
"name":"$name",
"olKey":"olkey for $name",
"birthDate": null,
"imageSmall": null,
"goodreadsUrl": "https://${name}.here",
"imageMedium":"image for $name",
"imageLarge": null
}
EOF

   # create author
   _create_author "${author_data}"
}




######
# print info for author
# 
######
print_author_info() {
    author_info="$1"
    name=$(echo "$author_info" | jq -r .name)
    id=$(echo "$author_info" | jq -r .id)

    echo "Author: '$name', ID: '$id'"
}



########
# delete all authors
#
# param: json with all authors
########
delete_all_authors() {
    # get 1000 authors
    authors=$(get_all_authors_with_offset_limit 0 1000 )
    ids=$(echo "${authors}" | jq -r ".data[].id" )
    num=$(echo "${authors}" | jq -r ".data | length" )

    echo "Delete all ($num) authors."

    for id in $ids
    do
        $(delete_author $id)
    done
}

###############
#
# Clean all authors
#
###############
author::clean() {
    echo ""
    delete_all_authors
}


###############
#
# Main test
#
###############
author::main_test() {
    echo "Get all authors"
    all_authors=$(get_all_authors)

    echo ""
    echo "----Delete all authors----"
    delete_all_authors

    echo ""
    echo "Create author: 'lengle'"
    author=$(create_author_lengle)
    authorname=$(echo "$author" | jq -r .name) 
    assert_string_equals "Madeleine LEngle" "$authorname" "Author name"

    echo ""
    echo "Create author: 'asimov'"
    author_asimov=$(create_author_asimov)
#    print_author_info "$author_asimov"
    author_asimov_id=$(echo "$author_asimov" | jq -r .id)
    authorname=$(echo "$author_asimov" | jq -r .name) 
    assert_string_equals "Isaac Asimov" "$authorname" "Author name"

    # verify author
    echo ""
    echo "Verifying first author"
    subjects=$(echo "$author_asimov" | jq -r '.subjects | join ("")')
    assert_equals $? 0 "jq failed on checking author subject"
    assert_string_equals "" "$subjects" "asimov subject list (should be empty)"

    echo ""
    echo "Get author by id $author_asimov_id "
    # get single author
    author_asimov_single=$(get_author_by_id "$author_asimov_id")
    print_author_info "$author_asimov_single"

    echo ""
    echo "Update author: 'asimov'"
    update_author $author_asimov_id

    echo "Verifying updated author..."
    author_asimov_single=$(get_author_by_id "$author_asimov_id")
    olkey=$(echo "$author_asimov_single" | jq -r .olKey)
    assert_string_equals "OL34221AXX" "$olkey" "Updated author olkey"

    echo ""
    echo "Get author by name (should be 1)"
    authors=$(get_author_by_name "asi" 0 100 )

    echo ""
    echo "Check limit & offset"
    limit=$(echo "$authors" | jq -r .limit)
    total=$(echo "$authors" | jq -r .total)
    offset=$(echo "$authors" | jq -r .offset)

    assert_equals 1 $limit "limit number authors"
    assert_equals 1 $total "total number authors"
    assert_equals 0 ${offset} "offset in authors returned"

    authors=$(echo "$authors" | jq -r .data)
    numAuthors=$(echo $authors | jq -r '. | length')
    assert_equals 1 $numAuthors "Number of authors"

    authorName=$(echo "$authors" | jq -r .[0].name)

    assert_string_equals "Isaac Asimov!"  "$authorName" "Author name"

    author::clean
}


###############
#
# Main test
#
###############
author::test_limit_offset() {
    echo ""
    echo "[[ Author Limit/Offset test ]]"

    # num authors to create
    COUNT=40

    echo "Creating $COUNT authors"

    idx=1
    while [ $idx -le $COUNT ]
    do
        idx=$(( $idx + 1 ))
        authorname="author_${idx}"
        result=$(create_author $authorname)
    done

    #######
    # Default returns
    # get authors and see how many
    echo ""
    echo "Testing default limit (20)"

    all_authors=$(get_all_authors)
    total=$(echo "$all_authors" | jq -r .total)
    offset=$(echo "$all_authors" | jq -r .offset)
    limit=$(echo "$all_authors" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in authors returned"
    assert_equals $EXPECTED_DEFAULT_LIMIT $limit "limit number authors"
    assert_equals $COUNT $total "total number authors"

    #######
    # new limit
    echo ""
    echo "Testing new limit"
    all_authors=$(get_all_authors_with_offset_limit 0 500)
    total=$(echo "$all_authors" | jq -r .total)
    offset=$(echo "$all_authors" | jq -r .offset)
    limit=$(echo "$all_authors" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in authors returned"
    assert_equals $COUNT $limit "limit number authors"
    assert_equals $COUNT $total "total number authors"


    #######
    # new offset
    echo ""
    echo "Testing new offset"
    all_authors=$(get_all_authors_with_offset_limit 10 10)
    total=$(echo "$all_authors" | jq -r .total)
    offset=$(echo "$all_authors" | jq -r .offset)
    limit=$(echo "$all_authors" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 10 ${offset} "offset in authors returned"
    assert_equals 10 $limit "limit number authors"
    assert_equals $COUNT $total "total number authors"

    #######
    # new offset
    echo ""
    echo "Testing 2nd new offset"
    all_authors=$(get_all_authors_with_offset_limit 13 2)
    total=$(echo "$all_authors" | jq -r .total)
    offset=$(echo "$all_authors" | jq -r .offset)
    limit=$(echo "$all_authors" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 13 ${offset} "offset in authors returned"
    assert_equals 2 $limit "limit number authors"
    assert_equals $COUNT $total "total number authors"


    ##########
    # Test with author name in query
    echo ""
    echo "Testing with author name in query"
    all_authors=$(get_author_by_name "author_1" 2 3 )

    total=$(echo "$all_authors" | jq -r .total)
    offset=$(echo "$all_authors" | jq -r .offset)
    limit=$(echo "$all_authors" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 2 ${offset} "offset in authors returned"
    assert_equals 3 $limit "limit number authors"
    assert_equals 10 $total "total number authors"


    author::clean

    echo "[[ Done Author Limit/Offset test ]]"
}



###############
#
# Test author endpoint
#
###############
test_author() {
    echo ""
    echo "[ Author test ]"

    author::main_test

    author::test_limit_offset

    echo "[ DoneAuthor test ]"
}
