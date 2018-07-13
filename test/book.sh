######################
#
# book related calls
#
#
######################



##########
# Create book in db
# 
##########
_create_book() {
    post_data="$1"

    ##########
    # create in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X POST "${ROOT_URL}/book" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making POST for test book"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        if [[ "$code" == "409" ]]; then
            error "Book already exists!"
        else
            error "Error making POST for test book. code: $code: $result"
        fi
    fi

    book_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$book_result"
}


#########
# Create book by asimov
#
#########
book::create_book_asimov() {
    asimov=$(create_author_asimov)
    asimov_id=$(echo "$asimov" | jq -r .id)

read -r -d '' book_data <<EOF
{
  "authorId": $asimov_id,
  "firstPublishedYear": 1952,
  "title": "The Currents Of Space",
  "isbns": [
    "9789867001122", "0449238296"
  ],
  "subjects": [
    "Science Fiction"
  ],
  "description": "High above the planet Florinia.",
  "openlibraryWorkUrl": "https://openlibrary.org/works/OL46385W",
  "goodreadsUrl": "string",
  "imageSmall": "https://covers.openlibrary.org/b/id/6297485-S.jpg",
  "imageMedium": "https://covers.openlibrary.org/b/id/6297485-M.jpg",
  "imageLarge": null
}
EOF

   # create book
   _create_book "${book_data}"
}

#########
# Create 2nd book
#
#########
book::create_book_second() {
    author=$(create_author_lengle)
    author_id=$(echo "$author" | jq -r .id)

read -r -d '' book_data <<EOF
{
  "authorId": $author_id,
  "firstPublishedYear": 1975,
  "title": "Currents are for eating",
  "description": "Low below the planet Florinia.",
  "openlibraryWorkUrl": "https://openlibrary.org/works/OL46385W",
  "goodreadsUrl": "fake string",
  "imageSmall": "small image location",
  "imageMedium": "https://covers.openlibrary.org/b/id/6297485-S.jpg",
  "imageLarge": null
}
EOF

   # create book
   _create_book "${book_data}"
}

###############
# Generic create book
#
# params:
# 1- name of book
# 2- author id
###############
book::create_book() {
#    title=$(url_encode "$1")
    title="$1"
    author_id="$2"


read -r -d '' book_data <<EOF
{
  "authorId": $author_id,
  "firstPublishedYear": 1999,
  "title": "$title",
  "description": "Book for $title.",
  "openlibraryWorkUrl": "https://openlibrary.org/works/$title",
  "goodreadsUrl": "fake string",
  "imageSmall": "small image location for ${title}",
  "imageMedium": "https://covers.openlibrary.org/b/id/${title}.jpg",
  "imageLarge": "large image location for ${title}"
}
EOF

   # create book
   _create_book "${book_data}"
}



##########
# get books by name
#
##########
get_books_by_title() {
    title="$1"
    query=$(url_encode "$title")

    # create url
    url="${ROOT_URL}/book?title=$query"

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
        error "Error making GET to /book for book '$bookName'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting book '$bookName'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}

##########
# get books by multiple ids
#
##########
get_books_by_multiple_ids() {
    ids="$1"
    query=$(url_encode "$ids")

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/book?book_id=$query" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /book for books '$ids'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting books by ids '$ids'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


##########
# get book by author ID
#
##########
get_books_by_author_id() {
    author_id="$1"
    query=$(url_encode "$author_id")

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/book?author_id=$query" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /book for author '$author_id'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting book. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


##########
# get book by author name
#
##########
get_books_by_author_name() {
    author_name="$1"
    query=$(url_encode "$author_name")

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/book?author_name=$query" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /book for book '$author_name'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting book. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}

##########
# get book by id
#
##########
get_book_by_id() {
    book_id="$1"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET ${ROOT_URL}/book/${book_id} \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /book for book '$bookName'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting book '$bookName'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


##########
# Get all books
#
##########
get_all_books() {
    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET ${ROOT_URL}/book\
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /book for all books"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting all books: $code: $error. http_code: $result."
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}

##########
# Get all books with offset and limit
#
##########
get_all_books_with_offset_limit() {
    offset=$1
    limit=$2

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X GET "${ROOT_URL}/book?limit=${limit}&offset=${offset}" \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making GET to /book for all books"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error getting all books: $code: $error. http_code: $result."
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    echo "$result"
}


########
# delete all books
#
# param: json with all books
########
delete_all_books() {
    # get 1000 books
    books=$(get_all_books_with_offset_limit 0 1000 )
    ids=$(echo "${books}" | jq -r ".data[].id" )
    num=$(echo "${books}" | jq -r ".data | length" )

    echo "Delete all ($num) books."

    for id in $ids
    do
        $(delete_book $id)
    done
}



##########
# delete book
#
# params:
# book id
##########
delete_book() {
    book_id="$1"

    result=$(curl -s -w 'CODE%{http_code}CODE' \
             -X DELETE ${ROOT_URL}/book/${book_id} \
             -H "authorization: $BEARER" \
             -H 'cache-control: no-cache' \
             -H 'content-type: application/json')
    if [ $? -ne 0 ]; then
        error "Error making DELETE to /book for book $book_id"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        echo "Error deleting book '$book_id'. code: $code: $error"
    fi

    # strip code
    result=$(echo "$result" | sed "s/CODE${code}CODE//g")
}



##########
# update book
#
#########
_update_book() {
    book_id="$1"
    post_data="$2"

logit "Book id: $book_id"

    ##########
    # create in database now
    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
             -X PUT "${ROOT_URL}/book/${book_id}" \
             -H 'content-type: application/json' \
             -H "authorization: $BEARER" \
             -d "$post_data" \
             -H 'cache-control: no-cache')
    if [ $? -ne 0 ]; then
        error "Error making PUT for test book"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" != "200" ]]; then
        error "Error making PUT for test book. code: $code: $result"
    fi

    book_result=$(echo "$result" | sed "s/CODE${code}CODE//g")
    logit "book updated"
}

##########
# Update book
#
# param: book id
##########
update_book() {
    book_id=$1

read -r -d '' book_data <<EOF
{
"description": "new description",
"imageSmall": "small image"
}
EOF

    _update_book $book_id "$book_data"
}


######
# print info for book
# 
######
print_book_info() {
    book_info="$1"
    title=$(echo "$book_info" | jq -r .title)
    id=$(echo "$book_info" | jq -r .id)
    author_name=$(echo "$book_info" | jq -r .authorName)

    echo "Book: '$title', ID: '$id', author: '$author_name'"
}


###############
#
# Clean books
#
###############
book::clean() {
    echo ""
    delete_all_books
    author::clean
}

###############
#
# Test the limits and offsets for large datasets
#
###############
book::test_limit_offset() {
    echo ""
    echo "[[ Book Limit/Offset test ]]"

    # create author for the generic books
    author=$(create_author_lengle)
    author_id=$(echo "$author" | jq -r .id)

    # num books to create
    COUNT=40

    echo "Creating $COUNT books"

    idx=1
    while [ $idx -le $COUNT ]
    do
        idx=$(( $idx + 1 ))
        bookname="book_${idx}"
        result=$(book::create_book $bookname $author_id)
    done

    #######
    # Default returns
    # get books and see how many
    echo ""
    echo "Testing default limit (20)"

    all_books=$(get_all_books)
    total=$(echo "$all_books" | jq -r .total)
    offset=$(echo "$all_books" | jq -r .offset)
    limit=$(echo "$all_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in books returned"
    assert_equals $EXPECTED_DEFAULT_LIMIT $limit "limit number books"
    assert_equals $COUNT $total "total number books"
    
    #######
    # new limit
    echo ""
    echo "Testing new limit (500)"
    all_books=$(get_all_books_with_offset_limit 0 500)
    total=$(echo "$all_books" | jq -r .total)
    offset=$(echo "$all_books" | jq -r .offset)
    limit=$(echo "$all_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in books returned"
    assert_equals $COUNT $limit "limit number books"
    assert_equals $COUNT $total "total number books"


    #######
    # new offset
    echo ""
    echo "Testing new offset (10)"
    all_books=$(get_all_books_with_offset_limit 10 10)
    total=$(echo "$all_books" | jq -r .total)
    offset=$(echo "$all_books" | jq -r .offset)
    limit=$(echo "$all_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 10 ${offset} "offset in books returned"
    assert_equals 10 $limit "limit number books"
    assert_equals $COUNT $total "total number books"

    #######
    # new offset
    echo ""
    echo "Testing 2nd new offset (13)"
    all_books=$(get_all_books_with_offset_limit 13 2)
    total=$(echo "$all_books" | jq -r .total)
    offset=$(echo "$all_books" | jq -r .offset)
    limit=$(echo "$all_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 13 ${offset} "offset in books returned"
    assert_equals 2 $limit "limit number books"
    assert_equals $COUNT $total "total number books"

    ##########
    # Test with book name in query
    echo ""
    echo "Testing with book title in query"
    all_books=$(get_books_by_title "book_1" 2 3)

    total=$(echo "$all_books" | jq -r .total)
    offset=$(echo "$all_books" | jq -r .offset)
    limit=$(echo "$all_books" | jq -r .limit)

    echo "Checking limit/offset/total"
    assert_equals 2 ${offset} "offset in books returned"
    assert_equals 3 $limit "limit number books"
    assert_equals 10 $total "total number books"

    book::clean

    echo "[[ Done Book Limit/Offset test ]]"
}

###############
# 
# Main book test
#
###############
book::main_test() {
    echo "Create first book"
    book=$(book::create_book_asimov)
#    print_book_info "$book"
    book_id=$(echo "$book" | jq -r .id)
    author_asimov_id=$(echo "$book" | jq -r .authorId)
    book_year=$(echo "$book" | jq -r .firstPublishedYear)
    assert_string_equals "1952" "$book_year" "Book's year"
    
    # get single book
    echo ""
    echo "Get single book"
    book=$(get_book_by_id $book_id)
    first_book_id=$(echo "$book" | jq -r .id)

    assert_equals "$book_id" "$first_book_id" "Get single book by id"

    # check isbns
    echo ""
    echo "Check isbns"
    num_isbns=$(echo "$book" | jq -r '.isbns | length')
    isbns=$(echo "$book" | jq -r '.isbns')
    assert_equals 2 $num_isbns "Number of isbns"
    assert_contains "$isbns" "9789867001122" "Isbn"
    assert_contains "$isbns" "0449238296" "Isbn"

    # check subjects
    echo ""
    echo "Check subjects"
    num_subjects=$(echo "$book" | jq -r '.subjects | length')
    assert_equals 1 $num_subjects "Number of subjects in book"


    # update book
    echo ""
    echo "Update book"
    update_book $book_id
    book=$(get_book_by_id $book_id)

    # verify description
    description=$(echo "$book" | jq -r .description )
    assert_string_equals "new description" "$description" "Updated book's description"

    imageSmall=$(echo "$book" | jq -r .imageSmall )
    assert_string_equals "small image" "$imageSmall" "Updated book's small image"

    # 2nd book
    echo "Create second book"
    book=$(book::create_book_second)
#    print_book_info "$book"
    second_book_id=$(echo "$book" | jq -r ".id")

    echo ""
    echo "Check isbns on 2nd book"

    num_isbns=$(echo "$book" | jq -r '.isbns | length')
    isbns=$(echo "$book" | jq -r '.isbns')
    assert_equals 0 $num_isbns "Number of isbns"

    num_subjects=$(echo "$book" | jq -r '.subjects | length')
    assert_equals 0 $num_subjects "Number of subjects"

    # check the null vs empty array
    ignore=$(echo "$book" | jq -r '.isbns | join(" ")')
    assert_equals $? 0 "jq failed on isbn query for 2nd book"

    ignore=$(echo "$book" | jq -r '.subjects | join(" ")')
    assert_equals $? 0 "jq failed on subject query for 2nd book"

    echo ""
    echo "Check all books"
    all_books=$(get_all_books)
    limit=$(echo "$all_books" | jq -r .limit)
    total=$(echo "$all_books" | jq -r .total)
    offset=$(echo "$all_books" | jq -r .offset)

    assert_equals 2 $limit "limit number books"
    assert_equals 2 $total "total number books"
    assert_equals 0 ${offset} "offset in books returned"

    # various queries
    echo ""
    echo "Test book queries"

    echo ""
    echo "By id, multiple"
    books=$(get_books_by_multiple_ids "$first_book_id,$second_book_id")
    # should be 2 books
    books=$(echo "$books" | jq -r .data)
    numBooks=$(echo $books | jq -r '. | length')
    assert_equals 2 $numBooks "Number books returned by ids"

    echo ""
    echo "By title, multiple"
    books=$(get_books_by_title "Currents")
    # should be 2 books
    books=$(echo "$books" | jq -r .data)
    numBooks=$(echo $books | jq -r '. | length')
    assert_equals 2 $numBooks "Number books returned by title"

    # get single book
    echo ""
    echo "Search by title, single book"
    books=$(get_books_by_title "Currents of space")

    echo ""
    echo "Check limit & offset for single book"

    offset=$(echo "$books" | jq -r .offset)
    limit=$(echo "$books" | jq -r .limit)
    total=$(echo "$books" | jq -r .total)
    assert_equals 1 $limit "limit number of books"
    assert_equals 1 $total "total number of books"
    assert_equals 0 $offset "Offset into books"

    title=$(echo "$books" | jq -r .data[0].title)
    assert_string_equals "The Currents Of Space" "$title" "Search by title"

    authors_name=$(echo "$books" | jq -r .data[0].authorName)
    assert_string_equals "Isaac Asimov" "$authors_name" "Search by title's author name"

    # by author id
    echo ""
    echo "By author id"
    books=$(get_books_by_author_id "$author_asimov_id")
    title=$(echo "$books" | jq -r .data[0].title)

    assert_string_equals "The Currents Of Space" "$title" "Query by author id's title"

    echo ""
    echo "Check limit & offset for query by id"
    offset=$(echo "$books" | jq -r .offset)
    limit=$(echo "$books" | jq -r .limit)
    total=$(echo "$books" | jq -r .total)
    assert_equals 1 $limit "limit number of books"
    assert_equals 1 $total "total number of books"
    assert_equals 0 $offset "Offset into books"

    # by author name
    echo ""
    echo "Check limit & offset for query by name"
    books=$(get_books_by_author_name "Isaac Asimo")

    offset=$(echo "$books" | jq -r .offset)
    limit=$(echo "$books" | jq -r .limit)
    total=$(echo "$books" | jq -r .total)
    assert_equals 1 $limit "limit number of books"
    assert_equals 1 $total "total number of books"
    assert_equals 0 $offset "Offset into books"

    echo ""
    echo "By author name"
    title=$(echo "$books" | jq -r .data[0].title)
    assert_string_equals "The Currents Of Space" "$title" "Query by author name"

    book::clean
}

###############
#
# Test book endpoint
#
###############
test_book() {
    echo ""
    echo "[ Book Test ]"

    book::main_test
    book::test_limit_offset

    echo "[ Done Book Test ]"

}
