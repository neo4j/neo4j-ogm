/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.persistence.examples.tree;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.tree.Entity;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 */
public class TreeIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.tree");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test // DATAGRAPH-731
    public void shouldCreateTreeProperly() {
        Entity parent = new Entity("parent");
        Entity child01 = new Entity("child01").setParent(parent);
        Entity child02 = new Entity("child02").setParent(parent);

        session.save(parent);

        session.clear();

        parent = session.load(Entity.class, parent.getId());
        assertThat(parent).isNotNull();
        assertThat(parent.getChildren()).hasSize(2);
        assertThat(parent.getParent()).isNull();
        List<String> childNames = new ArrayList<>();
        for (Entity child : parent.getChildren()) {
            childNames.add(child.getName());
            assertThat(child.getParent().getName()).isEqualTo(parent.getName());
        }
        assertThat(childNames.contains(child01.getName())).isTrue();
        assertThat(childNames.contains(child02.getName())).isTrue();
    }

    @Test // DATAGRAPH-731
    public void shouldLoadTreeProperly() {
        String cypher = "CREATE (parent:Entity {name:'parent'}) CREATE (child1:Entity {name:'c1'}) CREATE (child2:Entity {name:'c2'}) CREATE (child1)-[:REL]->(parent) CREATE (child2)-[:REL]->(parent)";
        session.query(cypher, Collections.emptyMap());
        session.clear();
        Entity parent = session.loadAll(Entity.class, new Filter("name", ComparisonOperator.EQUALS, "parent"))
            .iterator().next();
        assertThat(parent).isNotNull();
        assertThat(parent.getChildren()).hasSize(2);
        assertThat(parent.getParent()).isNull();
        List<String> childNames = new ArrayList<>();
        for (Entity child : parent.getChildren()) {
            childNames.add(child.getName());
            assertThat(child.getParent().getName()).isEqualTo(parent.getName());
        }
        assertThat(childNames.contains("c1")).isTrue();
        assertThat(childNames.contains("c2")).isTrue();
    }

    @Test // GH-88
    public void shouldMapElementsToTreeSetProperly() {
        String cypher = "CREATE (parent:Entity {name:'parent'}) CREATE (child1:Entity {name:'c1'}) CREATE (child2:Entity {name:'c2'}) CREATE (child1)-[:REL]->(parent) CREATE (child2)-[:REL]->(parent)";
        session.query(cypher, Collections.emptyMap());
        session.clear();
        Entity parent = session.loadAll(Entity.class, new Filter("name", ComparisonOperator.EQUALS, "parent"))
            .iterator().next();
        assertThat(parent).isNotNull();
        assertThat(parent.getChildren()).hasSize(2);
        assertThat(parent.getParent()).isNull();
        List<String> childNames = new ArrayList<>();
        for (Entity child : parent.getChildren()) {
            childNames.add(child.getName());
            assertThat(child.getParent().getName()).isEqualTo(parent.getName());
        }
        assertThat(childNames.get(0)).isEqualTo("c1");
        assertThat(childNames.get(1)).isEqualTo("c2");
    }
}
