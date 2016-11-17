#!/usr/bin/env bash

# Build nginx image

# image we build
imageName="wpff.books.nginx:latest"

# our real directory (so this can be called from outside directories)
ourDirectory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

docker build "$ourDirectory" -t $imageName

echo ""
echo "Built $imageName"
