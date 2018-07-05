#!/bin/bash

##################
#
# Test harness for books
#
#
#################

root_dir="$(cd -P "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

trap "exit 1" TERM
export TOP_PID=$$

# utilities
. ${root_dir}/util.sh

# tests
. ${root_dir}/author.sh
. ${root_dir}/user.sh
. ${root_dir}/tags.sh
. ${root_dir}/book.sh
. ${root_dir}/user_book.sh


#################
# Main function.
# 
#################
clean() {
    echo "*****************"
    echo "**    Clean    **"


    tag::clean

    user_book::clean

    # this cleans authors as well
    book::clean

    user::clean

    echo ""
    echo "*****************"

}


#################
# Main function.
# 
#################
main() {
    # Initialize
    initialize_variables

    # authorize
    authorize_admin

    # parse options
    while getopts "cx" opt; do
        case "$opt" in
            c)  clean
                exit 1
                ;;
            x)  NO_FAIL=1
                ;;
            \?) #unknown
                usage
                ;;
        esac
    done
    shift $(($OPTIND - 1))

    # clean first
    logit "Cleaning first."
    ignore=$(clean)

    test_tag
    test_user
    test_author
    test_book
    test_user_books

    echo ""
    echo "[[[ Done with all tests ]]]"
    echo "Passed $PASSED_TESTS tests"
}

# main
main "$@"
