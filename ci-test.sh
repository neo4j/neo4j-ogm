#!/usr/bin/env bash

################################################################################################################
# A simple script to run the OGM tests like the CI Server does.
#
# examples:
#
# run all tests using bolt driver and neo4j 3.0
#    ci-test -d bolt -v 3.0
#
# run a specific test using embedded driver and 2.3
#    ci-test -d embedded -v 2.3 -t ClassUtilsTest
#
# run using defaults (d=http, v=2.3, t=all tests)
#    ci-test
#
# if you omit an option, a default will be used (see below)
#
################################################################################################################
# defaults:
DRIVER="http"
VERSION="2.3"
TEST="*Test"

# set up the classpath
INSTALLED_LIBS="../../api/target/*:../../compiler/target/*:../../core/target/*:../../drivers/target/*:../../test/target/*"

################################################################################################################
function build() {
    echo "Packaging project..."
    mvn clean package -DskipTests=true -P"$VERSION","$DRIVER" >/dev/null 2>&1
    echo "Copying dependencies to test harness"
    mvn dependency:copy-dependencies >/dev/null 2>&1
}

################################################################################################################
function run-tests {
    pushd $1/target >/dev/null 2>&1
    find  test-classes -name "$TEST".class | sed -e "s/\.class//" -e "s/\//./g" -e "s/test-classes\.//" | xargs java -Dogm.properties="ogm-$DRIVER.properties" -cp $INSTALLED_LIBS:./dependency/* org.junit.runner.JUnitCore
    popd >/dev/null 2>&1
}

################################################################################################################
# options:
#  -v neo4j.version (major.minor), e.g. 2.3, corresponding to an existing maven profile, defaults to 2.3
#  -d driver  e.g. 'http', 'embedded', 'bolt', corresponding to an existing maven profile, defaults to http
#  -t test class to run, e.g. -tABTest Defaults to '*Test' - all tests )
while getopts ":v:d:t:" opt; do
    case "$opt" in
        t)  # test
            TEST="$OPTARG"
            ;;
        v)  # quiet
            VERSION="$OPTARG"
            ;;
        d)  # endpoint class
            DRIVER="$OPTARG"
            ;;
    esac
done


if [ "$DRIVER" = "bolt" ]
then
    VERSION="3.0"
fi


echo "------------------------------------------------------------------------------------"
echo "Running $TEST using driver: $DRIVER, Neo4j: $VERSION"
echo "------------------------------------------------------------------------------------"

build

#read -n 1 -s  # uncomment this if you want to wait for user input
run-tests "api"
run-tests "core"

# enable these tests when the test-fixture setup changes on the indexing branch are merged into master

#run-tests "embedded-driver"
#run-tests "http-driver"
#run-tests "bolt-driver"
