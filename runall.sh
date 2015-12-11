#!/usr/bin/env bash
mvn clean package -DskipTests=true
mvn dependency:copy-dependencies

INSTALLED_LIBS="../../api/target/*:../../compiler/target/*:../../core/target/*:../../drivers/target/*:../../test/target/*"

function run-tests {
    pushd $1/target
    find  test-classes -name *Test.class | sed -e "s/\.class//" -e "s/\//./g" -e "s/test-classes\.//" | xargs java -cp $INSTALLED_LIBS:./dependency/* org.junit.runner.JUnitCore
    popd
}

run-tests "api"
#run-tests "compiler"
run-tests "drivers"
#run-tests "test"
run-tests "core"