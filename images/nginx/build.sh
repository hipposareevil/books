#!/usr/bin/env bash

# Build nginx image

# image we build
imageName="books.frontend:latest"

# our real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker build "$our_directory" -t $imageName

echo ""
echo "Built $imageName"
