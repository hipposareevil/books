#################################
#
# Build file for golang projects
# 
#################################

###############
# Cleans a golang project.
#
# Run as a container in case directories are mapped as
# root and local (non docker) user doesn't have permissions.
# 
###############
golang::clean() {
    echo "[Removing dep vendor, metadata, and pkg directories]"
    
    docker run -it \
           -v ${our_directory}:/go \
           -w /go alpine \
           sh -c "rm -rf src/github.com/hipposareevil/vendor src/github.com/hipposareevil/Gopkg.* pkg/"
    
    echo "[Clean complete]"
}

##############
# Run 'dep' for dependencies
##############
golang::run_dep() {
    # Use 'dep' (https://github.com/golang/dep) to manage the  packages for this project.
    # This is done via the 'hipposareevil/alpine-dep' image
    # All packages are downloaded into the src/github.com/hipposareevil/vendor directory
    echo "[Running go's 'dep' against source]"
    docker run -it \
           -e GOPATH=/go \
           -v ${our_directory}:/go \
           -w /go hipposareevil/alpine-dep \
           init src/github.com/hipposareevil 
    if [ $? -ne 0 ]; then
        echo "** Error running 'dep' for  $image_name **"
        exit 1
    fi

    echo "[Done grabbing dependencies]"
    echo ""
}


###############
# Builds a golang project
# 
###############
golang::build() {
    golang::clean
    golang::run_dep
}

