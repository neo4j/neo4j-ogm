/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.persistence.abstraction;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.abstraction.*;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for entity abstractions
 */
public class EntityAbstractionTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        // #437 - Disabled AutoIndex must not break initialization because of Entity containing annotation @Id
        sessionFactory = new SessionFactory(getBaseConfiguration().build(), "org.neo4j.ogm.domain.abstraction");
    }

    @Before
    public void setUp() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldNotSaveEntityLabelAndMustRetrieveChildA() {
        ChildA a = new ChildA();
        a.setValue("ChildA");
        session.save(a);
        session.clear();

        assertThat(session.loadAll(Entity.class)).isEmpty();
        assertThat(session.load(Entity.class, a.getUuid())).isNull();

        ChildA dbA = session.load(ChildA.class, a.getUuid());
        assertThat(dbA).isNotNull();
        assertThat(dbA.getValue()).isEqualTo("ChildA");
    }

    @Test
    public void shouldNotSaveEntityLabelAndMustRetrieveChildAChildren() {
        ChildA a = new ChildA();
        ChildB b1 = new ChildB();
        ChildB b2 = new ChildB();
        ChildC c1 = new ChildC();
        a.add(b1);
        a.add(b2);
        a.add(c1);
        session.save(a);
        session.clear();

        assertThat(session.loadAll(Entity.class)).isEmpty();
        assertThat(session.loadAll(AnotherEntity.class)).isEmpty();
        a.getChildren().forEach(c -> assertThat(session.load(AnotherEntity.class, c.getUuid())).isNull());

        Set<AnotherEntity> children = session.load(ChildA.class, a.getUuid()).getChildren();
        assertThat(children).contains(b1, b2, c1);

//        FIXME - #414 - @PostLoad is not called in child overrided method (see ChildB)
//        children.stream().filter(c -> c instanceof ChildB).forEach(b -> assertThat(((ChildB) b).getValue()).isNotNull());
    }
}
