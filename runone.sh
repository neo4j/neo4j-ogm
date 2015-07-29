mvn clean package -DskipTests=true
mvn dependency:copy-dependencies
pushd target
java -cp ./neo4j-ogm-1.1.1-SNAPSHOT.jar:./neo4j-ogm-1.1.1-SNAPSHOT-tests.jar:./dependency/2.2.3/* org.junit.runner.JUnitCore $1
popd
