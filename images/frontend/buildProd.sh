#!/bin/bash

# image we build
imageName="books.frontend:prod"

# our real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Building $imageName"
docker build -f Dockerfile.prod -t $imageName $our_directory

echo ""
echo "Built $imageName"
