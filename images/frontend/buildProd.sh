#!/bin/bash

# image we build
imageName="books.frontend:prod"

# our real directory (so this can be called from outside directories)
our_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# build dev first
echo "Building the books.frontend:dev version first"
./buildDev.sh


echo "Now building $imageName"
# Run npm run build in container
docker run -it -v `pwd`/content/mybooks:/scratch books.frontend:dev npm run build

# now content/mybooks/dist is new

thedate=$(date)
echo "$thedate" > ${our_directory}/content/mybooks/dist/build.time

docker build -f Dockerfile.prod -t $imageName $our_directory

echo ""
echo "Built $imageName"
