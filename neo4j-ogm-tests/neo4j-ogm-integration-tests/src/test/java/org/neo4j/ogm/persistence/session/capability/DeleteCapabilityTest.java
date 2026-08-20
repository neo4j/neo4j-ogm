/*
 * Copyright (c) 2002-2026 "Neo4j,"
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
package org.neo4j.ogm.persistence.session.capability;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.domain.l1.Article;
import org.neo4j.ogm.domain.l1.Author;
import org.neo4j.ogm.domain.l1.Content;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author vince
 * @author Michael J. Simons
 */
public class DeleteCapabilityTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeAll
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music", "org.neo4j.ogm.domain.l1",
            "org.neo4j.ogm.domain.l3");
    }

    @BeforeEach
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    void shouldNotFailIfDeleteNodeEntityAgainstEmptyDatabase() {
        session.deleteAll(Album.class);
    }

    @Test
    void shouldNotFailIfDeleteRelationshipEntityAgainstEmptyDatabase() {
        session.deleteAll(Recording.class);
    }

    @Test
    void canDeleteSingleEntry() {
        Album album = new Album();
        session.save(album);
        assertEntityCount(1);

        session.delete(album);
        assertEntityCount(0);
    }

    @Test
    void canDeleteEntityCollection() {
        Album album1 = new Album();
        Album album2 = new Album();
        session.save(album1);
        session.save(album2);
        assertEntityCount(2);

        List<Object> albumList = new ArrayList<>();
        albumList.add(album1);
        albumList.add(album2);

        session.delete(albumList);
        assertEntityCount(0);
    }

    // GH-509
    @Test
    void canDeleteEntityArray() {
        Album album1 = new Album();
        Album album2 = new Album();
        session.save(album1);
        session.save(album2);
        assertEntityCount(2);

        List<Object> albumList = new ArrayList<>();
        albumList.add(album1);
        albumList.add(album2);

        session.delete(albumList.toArray());
        assertEntityCount(0);
    }

    private void assertEntityCount(int count) {
        session.clear(); // Ensure that no data is cached...
        long entityCount = session.countEntitiesOfType(Album.class);
        assertThat(entityCount).isEqualTo(count);
        session.clear(); // ...also for the subsequent calls in the test methods
    }

    @Test // L1
    void deleteFiltersAreSaveWithoutLabel() {
        Album album = new Album();
        session.save(album);
        assertEntityCount(1);

        session.query(
            "CREATE (a:Author {name: 'Michael'}) <-[:AUTHORED_BY]- (c:Article {name: 'JSpecify and NullAway: A fresh take on nullsafety in the Java world'})",
            Map.of());
        session.query("CREATE (a:Author {name: 'Bob'})",
            Map.of());

        session.clear();
        assertThat(session.countEntitiesOfType(Article.class)).isOne();
        assertThat(session.countEntitiesOfType(Author.class)).isEqualTo(2L);

        Filter filter = new Filter("name", ComparisonOperator.EQUALS, "Bob");
        filter.setNestedPropertyName("author");
        filter.setNestedPropertyType(Author.class);
        filter.setOwnerEntityType(Content.class);
        session.delete(Content.class, new Filters(filter), false);

        session.clear();
        assertEntityCount(1);
        assertThat(session.countEntitiesOfType(Article.class)).isOne();
        assertThat(session.countEntitiesOfType(Author.class)).isEqualTo(2L);
    }

    @Test // L3
    void deletionOfSameClassNameShouldNotDeleteWrongRecords() {

        session.query("CREATE (a:Order {name: 'Order A'}) RETURN id(a) AS id", Map.of());
        session.query("CREATE (a:SalesOrder {name: 'Order B'}) RETURN id(a) AS id", Map.of());

        session.delete(org.neo4j.ogm.domain.l3.b.Order.class, new Filters(), false);

        session.clear();
        assertThat(session.countEntitiesOfType(org.neo4j.ogm.domain.l3.a.Order.class)).isOne();
        assertThat(session.countEntitiesOfType(org.neo4j.ogm.domain.l3.b.Order.class)).isZero();
    }
}
