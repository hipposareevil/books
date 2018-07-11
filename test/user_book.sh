
######################
#
# user book related calls
#
#
######################

##########
# Create user book in db
# 
##########
_create_user_book() {
    post_data="$1"

    ##########
    # create in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X POST "${ROOT_URL}/user_book/${USER_ID}" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making POST for user_book"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        if [[ "$code" == "409" ]]; then
            error "Book already exists!"
        else
            error "Error making POST for user_book. code: $code, result: $result"
        fi
    fi

    book_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
#    logit "user_book created"
    echo "$book_result"
}


#########
# Create generic user_book
#
# params:
# 1- real book id
#########
create_generic_user_book() {
    book_id="$1"

    # fake tag will not be added
read -r -d '' book_data <<EOF
{
  "bookId": $book_id,
  "review" : "super review for generic book",
  "tags": [
    "testit"
  ]
}
EOF

   # create book
   _create_user_book "${book_data}"
}



#########
# Create user_book for asimov
#
# params:
# 1- real book id
#########
create_user_book_asimov() {
    book_id="$1"

read -r -d '' book_data <<EOF
{
  "bookId": $book_id,
  "rating": true,
  "review" : "review for asimov book",
  "tags": [
    "ebook", "sci-fi", "best"
  ]
}
EOF

   # create book
   _create_user_book "${book_data}"
}

#########
# Create user_book for second book
#
# params:
# 1- real book id
#########
create_user_book_second() {
    book_id="$1"

    # fake tag will not be added
read -r -d '' book_data <<EOF
{
  "bookId": $book_id,
  "rating": true,
  "review" : "review for second book",
  "tags": [
    "ebook", "fake", "super"
  ]
}
EOF

   # create book
   _create_user_book "${book_data}"
}




##########
# update user book
#
#########
_update_user_book() {
    user_book_id="$1"
    post_data="$2"

logit "UserBook id: $user_book_id"

    ##########
    # update in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X PUT "${ROOT_URL}/user_book/${USER_ID}/${user_book_id}" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making PUT for user book: $result"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        error "Error making PUT for user book. code: $code: $result"
    fi

    book_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    logit "user book updated"
    echo "$book_result"
}

##########
# Update user book
#
# param: user book id
##########
update_user_book() {
    user_book_id=$1

read -r -d '' book_data <<EOF
{
"tags": [
        "superbook", "fantasy", "best"
]
}
EOF

    _update_user_book $user_book_id "$book_data"
}

##########
# Update user book w/ a new review
#
# param: user book id
##########
update_user_book_new_review() {
    user_book_id=$1

read -r -d '' book_data <<EOF
{
"review": "new review"
}
EOF

    _update_user_book $user_book_id "$book_data"
}


########
# delete all user books
#
# param: json with all user books
########
delete_all_user_books() {
    # get 1000 books
    user_books=$(get_all_user_books_with_offset_limit 0 1000 )
    ids=$(echo "${user_books}" | jq -r ".data[].userBookId" )
    num=$(echo "${user_books}" | jq -r ".data | length" )

    echo "Delete all ($num) user_books."

    for id in $ids
    do
        $(delete_user_book $id)
    done
}


##########
# delete userbook
#
# params:
# userbook id
##########
delete_user_book() {
    book_id="$1"

#    logit "delete userbook $book_id"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X DELETE ${ROOT_URL}/user_book/${USER_ID}/${book_id} \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making DELETE to /userbook for userbook $book_id"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        echo "Error deleting userbook '$book_id'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
}


##########
# Get all user_books for this user
#
############
get_all_user_books() {
    result=$(curl -s -w 'CODE%{http_code}CODE' \
                  -X GET ${ROOT_URL}/user_book/${USER_ID} \
                  -H "authorization: $BEARER" \
                  -H 'cache-control: no-cache' \
                  -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /userbook for all user books"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting all user books: $code: $error. http_code: $result."
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}

##########
# Get all user_books for this user with offset and limit
#
############
get_all_user_books_with_offset_limit() {
    offset=$1
    limit=$2

    result=$(curl -s -w 'CODE%{http_code}CODE' \
                  -X GET "${ROOT_URL}/user_book/${USER_ID}?limit=${limit}&offset=${offset}" \
                  -H "authorization: $BEARER" \
                  -H 'cache-control: no-cache' \
                  -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /userbook for all user books"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting all user books with limit/offset: $code: $error. http_code: $result."
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


##########
# get user_book by id
#
# params:
# 1- user_book id
##########
get_user_book_by_id() {
    id="$1"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/user_book/${USER_ID}/${id} "\
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /user_book for userbook '$id'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting user_book '$id'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}

##########
# get user_book by title
#
# params:
# 1- title
#
# optionally takes offset/limit
##########
get_user_books_by_title() {
    title="$1"
    title=$(url_encode "$title")

    # create url
    url="${ROOT_URL}/user_book/${USER_ID}?title=$title"

    # if offset/limit were specified
    if [ $# -eq 3 ]; then
        offset="$2"
        limit="$3"
        url="${url}&offset=${offset}&limit=${limit}"
    fi

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${url}" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /user_book for title '$title'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting user_book by title '$title'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}

##########
# get user_book by title and tag
#
# params:
# 1- title
# 2- tag
# 
# optionally takes offset/limit
##########
get_user_books_by_title_and_tag() {
    title="$1"
    tag="$2"

    title=$(url_encode "$title")
    tag=$(url_encode "$tag")

    url="${ROOT_URL}/user_book/${USER_ID}?title=${title}&tag=${tag}"
    # if offset/limit were specified
    if [ $# -eq 4 ]; then
        offset="$3"
        limit="$4"
        url="${url}&offset=${offset}&limit=${limit}"
    fi

logit "URL URL: $url"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "$url"\
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /user_book for title '$title' and tag '$tag'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting user_book by title '$title' and tag '$tag'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


##########
# get user_book by tag
#
# params:
# 1- tag
##########
get_user_books_by_tag() {
    tag="$1"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/user_book/${USER_ID}?tag=${tag} "\
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /user_book for tag '$tag'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting user_book for tag '$tag'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}
##########
# get user_book by book_id
#
# params:
# 1- book_id
##########
get_user_book_by_bookid() {
    bookid="$1"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/user_book/${USER_ID}?book_id=${bookid} "\
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /user_book for bookid '$bookid'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting user_book for bookid '$bookid'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


###############
#
# Clean user books
#
###############
user_book::clean() {
    echo ""
    delete_all_user_books

    book::clean
    tag::clean
    user::clean
}


######
# print info for userbook
# 
######
print_userbook_info() {
    book_info="$1"
    title=$(echo "$book_info" | jq -r .title)
    id=$(echo "$book_info" | jq -r .userBookId)
    date=$(echo "$book_info" | jq -r .dateAdded)
    author_name=$(echo "$book_info" | jq -r .authorName)
    image_medium=$(echo "$book_info" | jq -r .imageMedium)

    echo "UserBook: '$title', UserBookID: '$id', author: '$author_name', added: '$date', medium: '$image_medium'"
}


#########
# Validate the first/asimov userbook
#
# params:
# 1- asimov book json
# 2- userbook json
########
validate_userbook_asimov() {
    asimov_book="$1"
    user_book="$2"

    echo ""
    echo "[Validating asimov user book]"
#    jqit "$user_book"
    print_userbook_info "$user_book"

    # get book info to validate against what is in user_book json
    asimov_book_id=$(echo "$asimov_book" | jq -r .id)
    asimov_book_title=$(echo "$asimov_book" | jq -r .title)
    asimov_book_author_name=$(echo "$asimov_book" | jq -r .authorName)
    asimov_book_author_id=$(echo "$asimov_book" | jq -r .authorId)
    asimov_book_year=$(echo "$asimov_book" | jq -r .firstPublishedYear)
    asimov_book_image_medium=$(echo "$asimov_book" | jq -r .imageMedium)
    asimov_book_image_small=$(echo "$asimov_book" | jq -r .imageSmall)

    # book id
    book_id=$(echo "$user_book" | jq -r .bookId)
    assert_equals $book_id $asimov_book_id "UserBook's mapped book ID"

    # title
    title=$(echo "$user_book" | jq -r .title)
    assert_string_equals "$asimov_book_title"  "$title" "UserBook's mapped book title"

    # author name
    authorname=$(echo "$user_book" | jq -r .authorName)
    assert_string_equals "$asimov_book_author_name" "$authorname"  "UserBook's mapped book author name"

    # author id
    authorid=$(echo "$user_book" | jq -r .authorId)
    assert_equals $authorid $asimov_book_author_id "UserBooks' mapped book author id"

    # rating
    rating=$(echo "$user_book" | jq -r .rating)
    assert_string_equals "true" "$rating" "UserBook's mapped book rating"

    # review
    review=$(echo "$user_book" | jq -r .review)
    assert_string_equals "review for asimov book" "$review" "UserBook's mapped review"

    # book year
    year=$(echo "$user_book" | jq -r .firstPublishedYear)
    assert_equals $year $asimov_book_year "UserBook's mapped book year"

    # book images
    image=$(echo "$user_book" | jq -r .imageMedium)
    assert_string_equals  "$asimov_book_image_medium" "$image" "UserBook's mapped book medium image"

    image=$(echo "$user_book" | jq -r .imageSmall)
    assert_string_equals "$asimov_book_image_small" "$image" "UserBook's mapped book small image"

    # tags
    tags=$(echo "$user_book" | jq -r '.tags | join(", ")')
    assert_contains "$tags" "ebook" "Userbook tags"
    assert_contains "$tags" "best" "Userbook tags"
    assert_contains "$tags" "sci-fi" "Userbook tags"

    echo "[Done validating asimov UserBook]"
    echo ""

}

###############
#
# Test the limits and offsets for large datasets
#
###############
user_book::test_limit_offset() {
    echo ""
    echo "[[ User Book Limit/Offset test ]]"

    # create tag for userbook
    result=$(tag::create_tag "testit")

    # create author for the generic books
    author=$(create_author_lengle)
    author_id=$(echo "$author" | jq -r .id)

    # num user books to create
    COUNT=40

    echo "Creating $COUNT books"

    idx=1
    while [ $idx -le $COUNT ]
    do
        idx=$(( $idx + 1 ))

        # create generic book
        book_title="book__${idx}"
        generic_book=$(book::create_book $book_title $author_id)
        book_id=$(echo $generic_book | jq -r .id)

        # create user_book now
        result=$(create_generic_user_book $book_id)
    done

    #####
    # by title
    echo ""
    echo "Testing query by title"

    all_user_books=$(get_user_books_by_title "book__1" 0 5)
    total=$(echo "$all_user_books" | jq -r .total)
    offset=$(echo "$all_user_books" | jq -r .offset)
    limit=$(echo "$all_user_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in userbooks returned"
    assert_equals 10 $total "total number userbooks"
    assert_equals 5 $limit "limit number userbooks"

    #####
    # by title and tag
    echo ""
    echo "Testing query by title and tag"

    all_user_books=$(get_user_books_by_title_and_tag "book__1" "testit" 0 5)
    total=$(echo "$all_user_books" | jq -r .total)
    offset=$(echo "$all_user_books" | jq -r .offset)
    limit=$(echo "$all_user_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in userbooks returned"
    assert_equals 5 $limit "limit number userbooks"
    assert_equals 10 $total "total number userbooks"

    #####
    # by title and tag #2
    echo ""
    echo "Testing query by title and missing tag"

    all_user_books=$(get_user_books_by_title_and_tag "book__1" "testitx" 0 5)
    total=$(echo "$all_user_books" | jq -r .total)
    offset=$(echo "$all_user_books" | jq -r .offset)
    limit=$(echo "$all_user_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in userbooks returned"
    assert_equals 5 $limit "limit number userbooks"
    assert_equals 10 $total "total number userbooks"



    #######
    # Default returns
    # get books and see how many
    echo ""
    echo "Testing default limit (20)"

    all_user_books=$(get_all_user_books)
    total=$(echo "$all_user_books" | jq -r .total)
    offset=$(echo "$all_user_books" | jq -r .offset)
    limit=$(echo "$all_user_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in userbooks returned"
    assert_equals $EXPECTED_DEFAULT_LIMIT $limit "limit number userbooks"
    assert_equals $COUNT $total "total number userbooks"

    
    #######
    # new limit
    echo ""
    echo "Testing new limit (500)"
    all_user_books=$(get_all_user_books_with_offset_limit 0 500)
    total=$(echo "$all_user_books" | jq -r .total)
    offset=$(echo "$all_user_books" | jq -r .offset)
    limit=$(echo "$all_user_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in user_books returned"
    assert_equals $COUNT $limit "limit number user_books"
    assert_equals $COUNT $total "total number user_books"


    #######
    # new offset
    echo ""
    echo "Testing new offset (10)"
    all_user_books=$(get_all_user_books_with_offset_limit 10 10)
    total=$(echo "$all_user_books" | jq -r .total)
    offset=$(echo "$all_user_books" | jq -r .offset)
    limit=$(echo "$all_user_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 10 ${offset} "offset in user_books returned"
    assert_equals 10 $limit "limit number user_books"
    assert_equals $COUNT $total "total number user_books"

    #######
    # new offset
    echo ""
    echo "Testing 2nd new offset (13)"
    all_user_books=$(get_all_user_books_with_offset_limit 13 2)
    total=$(echo "$all_user_books" | jq -r .total)
    offset=$(echo "$all_user_books" | jq -r .offset)
    limit=$(echo "$all_user_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 13 ${offset} "offset in user_books returned"
    assert_equals 2 $limit "limit number user_books"
    assert_equals $COUNT $total "total number user_books"

    user_book::clean

    echo "[[ DONE User Book Limit/Offset test ]]"
}



###############
#
#  Main test for user_book
#
###############
user_book::main_test() {
    # Create tags
    echo ""
    echo "Create tags"
    result=$(tag::create_tag "sci-fi")
    result=$(tag::create_tag "e-book")
    result=$(tag::create_tag "ebook")
    result=$(tag::create_tag "best")
    result=$(tag::create_tag "super")
    result=$(tag::create_tag "fantasy")

    echo ""
    echo "Create first book"
    asimov_book=$(book::create_book_asimov)
    asimov_book_id=$(echo "$asimov_book" | jq -r .id)
    print_book_info "$asimov_book"

    echo ""
    echo "Create user book for first book"
    asimov_user_book=$(create_user_book_asimov $asimov_book_id)
    asimov_user_book_id=$(echo "$asimov_user_book" | jq -r .userBookId)
    print_userbook_info "$asimov_user_book"

    ##### 
    # validate some things on first user book
    validate_userbook_asimov "$asimov_book" "$asimov_user_book"

    #######
    # validate user book after getting by id
    echo ""
    echo "Test getting single user book # $asimov_user_book_id"
    user_book=$(get_user_book_by_id "$asimov_user_book_id")
    validate_userbook_asimov "$asimov_book" "$user_book"


    ###########
    # create 2nd book
    echo "Create 2nd book"
    second_book=$(book::create_book_second)
    print_book_info "$second_book"

    echo ""
    echo "Create user book for second book"
    second_book_id=$(echo "$second_book" | jq -r .id)
    user_book=$(create_user_book_second $second_book_id)
    second_user_book_id=$(echo "$user_book" | jq -r .userBookId)

    # verify 'second' tag didn't make it in
    # There should only be 2 valid tag in the second book
    num_second_user_book_tags=$(echo "$user_book" | jq -r '.tags | length')
    assert_equals $num_second_user_book_tags 2 "Number of tags for 2nd book."

    echo ""
    echo "Test getting all userbooks"
    all_user_books=$(get_all_user_books)
    numBooks=$(echo $all_user_books | jq -r '.data | length')

    assert_equals $numBooks 2 "Number of all user books"

    # Test some queries
    echo ""
    echo "Test filter queries"

    echo "Test by title 'Current'"
    books=$(get_user_books_by_title "Current")
    numBooks=$(echo $books | jq -r '.data | length')
    assert_equals $numBooks 2 "Number of user books by title"

    echo ""
    echo "Test by title and tag"
    books=$(get_user_books_by_title_and_tag "Space" "Science Fiction")
#    jqit "$books"
    numBooks=$(echo $books | jq -r '.data | length')
    assert_equals $numBooks 1 "Number of user books by title and tag"

    echo ""
    echo "Check limit & offset"
    limit=$(echo "$books" | jq -r .limit)
    total=$(echo "$books" | jq -r .total)
    offset=$(echo "$books" | jq -r .offset)

    assert_equals 1 $limit "limit for books by title/tag"
    assert_equals 1 $total "total for books by title/tag"
    assert_equals 0 $offset "offset for books by title/tag"

    echo ""
    echo "Test by title and tag with same tag"
    books=$(get_user_books_by_title_and_tag "Current" "ebook")
#    jqit "$books" 
    numBooks=$(echo $books | jq -r '.data | length')
    assert_equals 2 $numBooks "Number of user books by title 'current' and tag 'ebook'"

    echo ""
    echo "Test by title and tag with same tag and only 1 matching title"
    books=$(get_user_books_by_title_and_tag "space" "ebook")
#    jqit "$books"
    numBooks=$(echo $books | jq -r '.data | length')
    assert_equals 1 $numBooks "Number of user books by title 'space' and tag 'ebook'"

    echo ""
    echo "Test by title try two"
    books=$(get_user_books_by_title "Of Space")
    numBooks=$(echo $books | jq -r '.data | length')

    assert_equals 1 $numBooks "Number of user books by title 'Of Space'"

    echo ""
    echo "Check limit & offset"
    limit=$(echo "$books" | jq -r .limit)
    total=$(echo "$books" | jq -r .total)
    offset=$(echo "$books" | jq -r .offset)

    assert_equals 1 $limit "limit for books by title/tag #2"
    assert_equals 1 $total "total for books by title/tag #2"
    assert_equals 0 $offset "offset for books by title/tag #2"


    echo ""
    echo "Test by book_id"
    books_by_book_id=$(get_user_book_by_bookid "$second_book_id")
    numBooks=$(echo $books_by_book_id | jq -r '.data | length')
    assert_equals 1 $numBooks "Number of user books by bookid '$second_book_id'"

    echo ""
    echo "Test by tag 'ebook'"
    books_by_tag=$(get_user_books_by_tag "ebook")
    numBooks=$(echo $books_by_tag | jq -r '.data | length')
    assert_equals 2 $numBooks "Number of user books by by tag 'ebook'"

    echo ""
    echo "Test by multiple tag"
    books_by_tag=$(get_user_books_by_tag "super&tag=best")
    numBooks=$(echo $books_by_tag | jq -r '.data | length')

    assert_equals 2 $numBooks "Number of user books by by tags 'super & ebook'"

    echo ""
    echo "Test by single tag 'sci-fi'"
    books_by_tag=$(get_user_books_by_tag "sci-fi")
    numBooks=$(echo $books_by_tag | jq -r '.data | length')
    assert_equals 1 $numBooks "Number of user books by by tag 'sci-fi'"

    echo ""
    echo "Delete 2nd user book"
    delete_user_book "$second_user_book_id"

    all_user_books=$(get_all_user_books)
    numBooks=$(echo $all_user_books | jq -r '.data | length')
    assert_equals 1 $numBooks "Number of user books after deleting 2nd user book"

    echo ""
    echo "Update first book. adding superbook & fantasy to tags"
    updated_book=$(update_user_book "$asimov_user_book_id")

    echo ""
    echo "check tags on updated book"
    tags=$(echo "$updated_book" | jq -r '.tags | join(", ")')
    assert_contains "$tags" "fantasy" "Userbook tags"
    assert_contains "$tags" "best" "Userbook tags"


    echo ""
    echo "Update first book by changing review."
    updated_book=$(update_user_book_new_review "$asimov_user_book_id")

    echo "check tags on updated book"
    echo ""
    tags=$(echo "$updated_book" | jq -r '.tags | join(", ")')
    assert_contains "$tags" "fantasy" "Userbook tags"
    assert_contains "$tags" "best" "Userbook tags"

    echo ""
    echo "check review on updated book"
    review=$(echo "$updated_book" | jq -r '.review')
    assert_string_equals "new review" "$review" "Userbooks review"

    user_book::clean
}

###############
# Test the user permissions
# 
# Create a new user, get authentication for it,
# create a user_book for admin user and try to modify it
# via the 2nd user. This should fail.
# Gets should pass.
# 
###############
 user_book::test_user_permissions() {
     echo ""
     echo "[ User Book permission test ]"

     # create userbook for admin/1
     # create new user
     # get bearer
     # try to update userbook
     
     ####
     # create tag, author, book, etc
     echo ""
     echo "Create tag"
     result=$(tag::create_tag "testit")
     echo "Create author"
     author=$(create_author_lengle)
     author_id=$(echo "$author" | jq -r .id)
     
     book_title="generic book for permission test"
     echo "Create book '$book_title' for author '$author_id'"
     generic_book=$(book::create_book "$book_title" "$author_id")
     book_id=$(echo $generic_book | jq -r .id)

     admin_user_book=$(create_generic_user_book $book_id)
     admin_user_bookid=$(echo "$admin_user_book" | jq -r .userBookId)

     ####
     # new user
     second_user=$(user::create_user "notadmin")
     authorize_second_user "notadmin" "${SECOND_USER_PASSWORD}"

     ######
     # try to update the admin_user_book

     # update user book
read -r -d '' post_data <<EOF
{
"tags": [
        "bad", "tag"
]
}
EOF
echo ""
     echo "Trying to update user book $admin_user_bookid, this should fail."
     result=$(curl -s  -w 'CODE%{http_code}CODE'  \
                   -X PUT "${ROOT_URL}/user_book/${USER_ID}/${admin_user_bookid}" \
                   -H 'content-type: application/json' \
                   -H "authorization: $BEARER_second" \
                   -d "$post_data" \
                   -H 'cache-control: no-cache')
     if [ $? -ne 0 ]; then
         error "Error making PUT for user book: $result"
     fi

     # get code
     code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)

     # code should not be 200
     assert_not_equals 200 $code "HTTP error code when updating admin's userbook"

     #########
     # create user book
     echo ""
     echo "Trying to create user book for admin, this should fail."
     result=$(curl -s  -w 'CODE%{http_code}CODE'  \
                   -X POST "${ROOT_URL}/user_book/${USER_ID}" \
                   -H 'content-type: application/json' \
                   -H "authorization: $BEARER_second" \
                   -d "$post_data" \
                   -H 'cache-control: no-cache')
     if [ $? -ne 0 ]; then
         error "Error making POST for user book: $result"
     fi

     # get code
     code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)

     # code should not be 200
     assert_not_equals 200 $code "HTTP error code when creating a userbook for admin user"

     #########
     # delete user book
     echo ""
     echo "Trying to delete user book $admin_user_bookid, this should fail."
     result=$(curl -s  -w 'CODE%{http_code}CODE'  \
                   -X DELETE "${ROOT_URL}/user_book/${USER_ID}/${admin_user_bookid}" \
                   -H 'content-type: application/json' \
                   -H "authorization: $BEARER_second" \
                   -H 'cache-control: no-cache')
     if [ $? -ne 0 ]; then
         error "Error making DELETE for user book: $result"
     fi

     # get code
     code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)

     # code should not be 200
     assert_not_equals 200 $code "HTTP error code when deleting admin's userbook"

     #########
     # get user book
     echo ""
     echo "Trying to GET user book ${admin_user_bookid}, should succeed."
     url="${ROOT_URL}/user_book/${USER_ID}/${admin_user_bookid}"
     result=$(curl -s  -w 'CODE%{http_code}CODE'  \
                   -X GET "$url" \
                   -H 'content-type: application/json' \
                   -H "authorization: $BEARER_second" \
                   -H 'cache-control: no-cache')
     if [ $? -ne 0 ]; then
         error "Error making GET for user book: $result"
     fi

     # get code
     code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)

     # code should be 200
     assert_equals 200 $code "HTTP error code when GETing admin's single userbook"

     #########
     # get all user book
     echo ""
     echo "Trying to GET all user books for admin, should succeed."
     result=$(curl -s  -w 'CODE%{http_code}CODE'  \
                   -X GET "${ROOT_URL}/user_book/${USER_ID}" \
                   -H 'content-type: application/json' \
                   -H "authorization: $BEARER_second" \
                   -H 'cache-control: no-cache')
     if [ $? -ne 0 ]; then
         error "Error making GET for all user book: $result"
     fi

     # get code
     code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)

     # code should be 200
     assert_equals 200 $code "HTTP error code when GETing all admin's userbooks"

     # clean up
     user_book::clean

     echo "[ End User Book permission test ]"
 }


###############
#
# Test user_book endpoint
#
###############
test_user_books() {
    echo ""
    echo "[ User Book test ]"
    
    user_book::main_test

    user_book::test_limit_offset

    user_book::test_user_permissions


    echo "[ Done User Book test ]"
}
