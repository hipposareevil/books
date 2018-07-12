
######################
#
# review related calls
#
#
######################

##################
#
# Get reviews
#
# params:
# 1- (optional) bearer string
##################
get_reviews_for_book() {
    bearer="$BEARER"
    book_id=$1

    # if bearer string is specified, use it, otherwise
    # we default to the original
    if [ $# -eq 2 ]; then
        bearer="$2"
    fi

    result=$(curl -s  -w 'CODE%{http_code}CODE'  \
                   -X GET "${ROOT_URL}/review/${book_id}" \
                   -H 'content-type: application/json' \
                   -H "authorization: ${bearer}" \
                   -d "$post_data" \
                   -H 'cache-control: no-cache')

    if [ $? -ne 0 ]; then
        error "Error making GET to /review for book '$book_id'"
    fi

    # get code
    code=$(echo "$result" |  awk -FCODE '{print $2}' | xargs)
    if [[ "$code" -ne "200" ]]; then
        error "Error making GET to /review for book '$book_id': code: $code, error: $error"
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
review::clean() {
    echo ""
    delete_all_user_books

    book::clean
    tag::clean
    user::clean
}


##############
#
# main test
#
#
##############
review::main_test() {
     echo "[ Review main test ]"


    # create userbook for admin/1
    # create new user
    
    ####
    # create tag, author, book, etc
    echo ""
    echo "Create tag"
    result=$(tag::create_tag "testit")
    echo "Create author"
    author=$(create_author_lengle)
    author_id=$(echo "$author" | jq -r .id)
    
    book_title="generic book for review test"
    echo "Create book '$book_title' for author '$author_id'"
    generic_book=$(book::create_book "$book_title" "$author_id")
    book_id=$(echo $generic_book | jq -r .id)

    admin_user_book=$(create_generic_user_book $book_id)

    # Create second user to get reviews from
    second_user=$(user::create_user "notadmin")
    authorize_second_user "notadmin" "${SECOND_USER_PASSWORD}"

    # get reviews as 2nd user
    reviews=$(get_reviews_for_book $book_id "$BEARER_second")

    # Validate review
    total=$(echo "$reviews" | jq -r .total)
    offset=$(echo "$reviews" | jq -r .offset)
    limit=$(echo "$reviews" | jq -r .limit)

    echo ""
    echo "Checking limit/offset/total"
    assert_equals 0 ${offset} "offset in reviews returned"
    assert_equals 1 $total "total number reviews"
    assert_equals 1 ${limit} "limit number reviews"

    num=$(echo "${reviews}" | jq -r ".data | length" )
    assert_equals 1 $num "Number of reviews"

    first_review=$(echo "$reviews" | jq -r '.data[0]')

    echo ""
    echo "Checking review contents"
    review_books_id=$(echo "$first_review" | jq -r .bookId)
    review_rating=$(echo "$first_review" | jq -r .rating)
    review_review=$(echo "$first_review" | jq -r .review)
    review_tags=$(echo "$first_review" | jq -r '.tags | join(", ")')
    review_user=$(echo "$first_review" | jq -r .userName)

    assert_equals $bookId $review_books_id "Review's book id"
    assert_string_equals "false" $review_rating "Review's rating"
    assert_string_equals "super review for generic book" "$review_review"  "Review's review"
    assert_string_equals "admin" "$review_user"  "Review's user"
    assert_contains "$review_tags" "testit" "Review's tags"


    # clean up
    review::clean
    echo "[ End review main test ]"
}


###############
#
# Test review endpoint
#
###############
test_reviews() {
    echo ""
    echo "[ Review test ]"
    
    review::main_test

    echo "[ Done Review test ]"
}
