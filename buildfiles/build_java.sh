#################################
#
# Build file for java projects.
# 
# This is sourced from the main 'build-services.sh'
# build file.
#
# 
#################################

#############
# Copy common jars into a local lib directory to be
# accessible for a docker version of maven or gradle
#
#############
_copy_common_jars() {
    rm -rf /tmp/lib
    mkdir -p /tmp/lib
    mkdir -p ${our_directory}/lib
    cp ${our_directory}/../../mybooks_common/repos/*.jar ${our_directory}/lib/
    cp ${our_directory}/../../mybooks_common/repos/*.jar /tmp/lib
}


#########
# Run a maven command
#
# params:
# 1- command to run
#########
_maven() {
    command=$@

  # See if mvn is already installed
    which mvn > /dev/null
    if [ $? -eq 0 ]; then
        echo "[[Using local maven]]"
        (cd $our_directory; mvn $command)
    else
        echo "[[Running maven via docker]]"
        ###
        # build project via maven using a docker container
        # volumes:
        # our_directory ==> /usr/src/mymaven   (source)
        # .m2          ==> /opt/.m2           (maven repo cache)
        docker run --rm \
               -it \
               -v "$our_directory":/usr/src/mymaven \
               -v "${our_directory}/lib":/tmp/lib \
               -v "$our_directory"/../../.m2:/opt/.m2 \
               -w /usr/src/mymaven \
               maven:3.3.9-jdk-8-alpine \
               mvn \
               -Dmaven.repo.local=/opt/.m2/ \
               $command
        build_result=$?
    fi
    # return result of build
    return $build_result
}

#############
# Build maven
#
#############
build_maven() {
    _maven "package"
    return $?
}



#############
# Load our mybooks_common
#
#############
load_common_jars_for_maven() {
    echo "Loading mybooks-common into project"
#    _maven install:install-file -Dfile=${our_directory}/lib/mybooks_common-1.0.jar -DgroupId=com.wpff.common -DartifactId=mybooks-common -Dversion=1.0 -Dpackaging=jar
    _maven install:install-file -Dfile=/tmp/lib/mybooks_common-1.0.jar -DgroupId=com.wpff.common -DartifactId=mybooks-common -Dversion=1.0 -Dpackaging=jar
    if [ $? -ne 0 ]; then
        echo "Unable to load mybooks_common.jar into project '$project'"
        exit 1
    fi       
}


#############
# Clean maven
#
#############
clean_maven() {
    _maven "clean"
    return $?
}


############
# Run a gradle command
#
# params:
# 1- command to run
############
_gradle() {
    command=$1

    which gradle > /dev/null
    if [ $? -eq 0 ]; then
        echo "[[Using local gradle]]"
        (cd $our_directory; gradle "$command")
        build_result=$?
    else
        echo "[[Running gradle via docker]]"
        ###
        # build project via gradle using a docker container
        # volumes:
        # our_directory ==> /work          (source)
        # .gradle      ==> /GRADLE_CACHE  (gradle repo cache)
        docker run --rm \
               -it \
               -v "$our_directory":/work \
               -v "$our_directory"/../../.gradle:/GRADLE_CACHE \
               hipposareevil/alpine-gradle \
               ""$command""
        build_result=$?
    fi
    # return result of command
    return $build_result
}

#############
# Build gradle
#
#############
build_gradle() {
    _gradle "build"
    return $?
}

#############
# Clean gradle
#
#############
clean_gradle() {
    _gradle "clean"
    return $?
}

###############
# Cleans a java project
# 
###############
java::clean() {
    # build project
    if [ -e $our_directory/pom.xml ]; then
        clean_maven
    elif [ -e $our_directory/build.gradle ]; then
        clean_gradle
    else 
        echo "Unable to find maven or gradle artifacts to perform the clean."
        echo "Exiting"
        echo 1
    fi

    if [ $? -ne 0 ]; then
        echo "Unable to clean the project. Exiting"
        exit 1
    fi
}


###############
# Builds a java project
# 
###############
java::build() {
    # copy in common
    _copy_common_jars

    # build project
    local then=$(date +%s)

    if [ -e $our_directory/pom.xml ]; then
        load_common_jars_for_maven
        build_maven
    elif [ -e $our_directory/build.gradle ]; then
        build_gradle
    else 
        echo "[[Unable to find maven or gradle artifacts to build.]]"
        echo "[[Exiting]]"
        echo 1
    fi

    build_success=$?
    if [ $build_success -ne 0 ]; then
        echo "[[Unable to build successfully.]]"
        echo "[[Exiting]]"
        exit 1
    fi

    local now=$(date +%s)
    local elapsed=$(expr $now - $then)
    echo "[[Built application in $elapsed seconds]]"

    if [ $? -ne 0 ]; then
        echo "[[Unable to build project. Exiting.]]"
        exit 1
    fi
}
