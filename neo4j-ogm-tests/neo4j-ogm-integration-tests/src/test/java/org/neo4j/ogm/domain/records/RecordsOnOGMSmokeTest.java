/*
 * Copyright (c) 2002-2023 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.records;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.ogm.context.SingleUseEntityMapper;
import org.neo4j.ogm.metadata.reflect.ReflectionEntityInstantiator;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

public class RecordsOnOGMSmokeTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private static long generatedId;

    @BeforeAll
    public static void oneTimeSetUp() {
        var driver = getDriver();
        var nativeDriver = driver.unwrap(Driver.class);

        nativeDriver.executableQuery("""
                CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})
                CREATE (Keanu:Person {name:'Keanu Reeves', born:1964})
                CREATE (Carrie:Person {name:'Carrie-Anne Moss', born:1967})
                CREATE (Laurence:Person {name:'Laurence Fishburne', born:1961})
                CREATE (Hugo:Person {name:'Hugo Weaving', born:1960})
                CREATE (LillyW:Person {name:'Lilly Wachowski', born:1967})
                CREATE (LanaW:Person {name:'Lana Wachowski', born:1965})
                CREATE (JoelS:Person {name:'Joel Silver', born:1952})
                CREATE (KevinB:Person {name:'Kevin Bacon', born:1958})
                CREATE
                (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrix),
                (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrix),
                (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrix),
                (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrix),
                (LillyW)-[:DIRECTED]->(TheMatrix),
                (LanaW)-[:DIRECTED]->(TheMatrix),
                (JoelS)-[:PRODUCED]->(TheMatrix)

                CREATE (Emil:Person {name:"Emil Eifrem", born:1978})
                CREATE (Emil)-[:ACTED_IN {roles:["Emil"]}]->(TheMatrix)

                CREATE (TheMatrixReloaded:Movie {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})
                CREATE
                (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrixReloaded),
                (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrixReloaded),
                (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrixReloaded),
                (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrixReloaded),
                (LillyW)-[:DIRECTED]->(TheMatrixReloaded),
                (LanaW)-[:DIRECTED]->(TheMatrixReloaded),
                (JoelS)-[:PRODUCED]->(TheMatrixReloaded)

                CREATE (TheMatrixRevolutions:Movie {title:'The Matrix Revolutions', released:2003, tagline:'Everything that has a beginning has an end'})
                CREATE
                (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrixRevolutions),
                (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrixRevolutions),
                (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrixRevolutions),
                (KevinB)-[:ACTED_IN {roles:['Unknown']}]->(TheMatrixRevolutions),
                (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrixRevolutions),
                (LillyW)-[:DIRECTED]->(TheMatrixRevolutions),
                (LanaW)-[:DIRECTED]->(TheMatrixRevolutions),
                (JoelS)-[:PRODUCED]->(TheMatrixRevolutions)
                """
            )
            .execute();
        generatedId = nativeDriver
            .executableQuery("CREATE (r:RecordWithGeneratedId {name: 'Look, I am using a deprecated id'}) RETURN id(r)")
            .execute().records().get(0).get(0).asLong();
        nativeDriver
            .executableQuery(
                "CREATE (r:RecordWithDynamicLabels:Foo:Bar {id: 4711, name: 'Look, more labels'}) RETURN id(r)")
            .execute();

        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.records");
    }

    @Test
    public void simpleRecordsShouldBePopulated() {

        var people = sessionFactory.openSession().loadAll(Person.class);
        assertThat(people).isNotEmpty();
        assertThat(people).extracting(Person::name).noneMatch(Objects::isNull);
    }

    @Test
    void generatedIdsShallBePopulated() {

        var things = sessionFactory.openSession().loadAll(RecordWithGeneratedId.class);
        assertThat(things).first().matches(r -> r.id() == generatedId && r.name() != null);
    }

    @Test
    void dynamicLabelsShallBePopulated() {

        var thing = sessionFactory.openSession().load(RecordWithDynamicLabels.class, 4711L);
        assertThat(thing).isNotNull();
        assertThat(thing.id()).isEqualTo(4711L);
        assertThat(thing).extracting(RecordWithDynamicLabels::additionalLabels).asList()
            .containsExactlyInAnyOrder("Foo", "Bar");
    }

    @Test
    void sensibleRecordsMightBeUsedForWritingToo() {
        var session = sessionFactory.openSession();
        var driver = sessionFactory.unwrap(Driver.class);

        var thing = new RecordWithDynamicLabels(23, "Some record", List.of());
        session.save(thing);

        var n = driver.executableQuery("MATCH (n:RecordWithDynamicLabels {id: $id}) RETURN n")
            .withParameters(Map.of("id", 23))
            .execute().records().get(0).get("n");
        assertThat(n).isNotNull();
        var node = n.asNode();
        assertThat(node.get("id").asLong()).isEqualTo(23L);
        assertThat(node.get("name").asString()).isEqualTo("Some record");

        thing = new RecordWithDynamicLabels(23, "Some updated record", List.of("Baz"));
        session.save(thing);

        n = driver.executableQuery("MATCH (n) WHERE id(n) = $id RETURN n").withParameters(Map.of("id", node.id()))
            .execute().records().get(0).get("n");
        assertThat(n).isNotNull();
        node = n.asNode();
        assertThat(node.get("id").asLong()).isEqualTo(23L);
        assertThat(node.get("name").asString()).isEqualTo("Some updated record");
        assertThat(node.labels()).containsExactlyInAnyOrder("RecordWithDynamicLabels", "Baz");
    }

    @Test
    void usingRelationshipsWithRecordsShouldNotBurnEverythingToTheGround() {

        var session = sessionFactory.openSession();
        var movies = session.loadAll(Movie.class);
        assertThat(movies).isNotEmpty();

        var theMatrix = movies.stream()
            .filter(m -> "The Matrix".equals(m.title()))
            .findFirst().orElseThrow();
        assertThat(theMatrix.actors()).isNotEmpty().allSatisfy(actor -> {
            assertThat(actor.id()).isNotNull();
            assertThat(actor.movie()).isSameAs(theMatrix);
        });
    }

    @Test
    void singleUseEntityMapperMightNotGoNutsEither() {

        var entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query(
                "MATCH (r:RecordWithGeneratedId) WHERE id(r) = $id RETURN r AS result",
                Map.of("id", generatedId))
            .queryResults();
        assertThat(results).hasSize(1);

        GenericQueryResultWrapper<RecordWithGeneratedId> thing = entityMapper.map(GenericQueryResultWrapper.class,
            results.iterator().next());
        assertThat(thing).isNotNull();
        assertThat(thing).extracting(GenericQueryResultWrapper::result)
            .extracting("id", "name")
            .containsExactly(generatedId, "Look, I am using a deprecated id");
    }
}
