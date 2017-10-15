#!/usr/bin/env bash

# image we build
imageName="books.frontend:dev"

# our real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker build "$our_directory" -t $imageName

echo ""
echo "Built $imageName"
