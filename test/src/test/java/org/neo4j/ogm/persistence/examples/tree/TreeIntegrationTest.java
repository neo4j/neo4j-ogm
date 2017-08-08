/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.persistence.examples.tree;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.tree.Entity;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class TreeIntegrationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.tree");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    /**
     * @see DATAGRAPH-731
     */
    @Test
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

    /**
     * @see DATAGRAPH-731
     */
    @Test
    public void shouldLoadTreeProperly() {
        String cypher = "CREATE (parent:Entity {name:'parent'}) CREATE (child1:Entity {name:'c1'}) CREATE (child2:Entity {name:'c2'}) CREATE (child1)-[:REL]->(parent) CREATE (child2)-[:REL]->(parent)";
        session.query(cypher, Utils.map());
        session.clear();
        Entity parent = session.loadAll(Entity.class, new Filter("name", ComparisonOperator.EQUALS, "parent")).iterator().next();
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

    /**
     * @see Issue 88
     */
    @Test
    public void shouldMapElementsToTreeSetProperly() {
        String cypher = "CREATE (parent:Entity {name:'parent'}) CREATE (child1:Entity {name:'c1'}) CREATE (child2:Entity {name:'c2'}) CREATE (child1)-[:REL]->(parent) CREATE (child2)-[:REL]->(parent)";
        session.query(cypher, Utils.map());
        session.clear();
        Entity parent = session.loadAll(Entity.class, new Filter("name", ComparisonOperator.EQUALS, "parent")).iterator().next();
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
