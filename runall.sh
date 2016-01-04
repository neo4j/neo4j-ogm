#!/usr/bin/env bash

# build and package with dependencies. This is done once only
mvn clean package -DskipTests=true
mvn dependency:copy-dependencies

# set up the classpath
INSTALLED_LIBS="../../api/target/*:../../compiler/target/*:../../core/target/*:../../drivers/target/*:../../test/target/*"

# set up the configurations. All tests will run for each of these configurations
CONFIGURATIONS=(
    "ogm-http.properties"
    "ogm-embedded.properties"
)

function run-tests {
    pushd $1/target
    find  test-classes -name *Test.class | sed -e "s/\.class//" -e "s/\//./g" -e "s/test-classes\.//" | xargs java -Dogm.properties="$CONFIGURATION" -cp $INSTALLED_LIBS:./dependency/* org.junit.runner.JUnitCore
    popd
}

for CONFIGURATION in "${CONFIGURATIONS[@]}"
do
    echo "Running tests using configuration: $CONFIGURATION"
    #read -n 1 -s  # uncomment this if you want to wait for user input
    run-tests "api"
    run-tests "drivers"
    run-tests "core"
done