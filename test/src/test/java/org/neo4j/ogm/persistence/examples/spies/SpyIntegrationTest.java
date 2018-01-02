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

package org.neo4j.ogm.persistence.examples.spies;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.spies.Spy;
import org.neo4j.ogm.domain.spies.Target;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class SpyIntegrationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.spies");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    /**
     * @see DATAGRAPH-728
     */
    @Test
    public void shouldSaveAndLoadSpyInEachDirection() {
        Spy mata = new Spy("Mata Hari");
        Spy julius = new Spy("Julius Rosenberg");

        Target mataJulius = new Target();
        mataJulius.setSpy(mata);
        mataJulius.setTarget(julius);
        mataJulius.setCode("Hawk");

        Target juliusMata = new Target();
        juliusMata.setSpy(julius);
        juliusMata.setTarget(mata);
        juliusMata.setCode("Robin");

        mata.setSpiesOn(mataJulius);
        mata.setSpiedOnBy(juliusMata);
        julius.setSpiesOn(juliusMata);
        julius.setSpiedOnBy(mataJulius);

        session.save(mata);
        session.save(julius);

        session.clear();

        mata = session.load(Spy.class, mata.getId());
        assertThat(mata).isNotNull();
        assertThat(mata.getSpiesOn().getTarget().getName()).isEqualTo(julius.getName());
        assertThat(mata.getSpiedOnBy().getCode()).isEqualTo("Robin");
        assertThat(mata.getSpiedOnBy().getSpy().getName()).isEqualTo(julius.getName());

        session.clear();
        julius = session.load(Spy.class, julius.getId());
        assertThat(julius).isNotNull();
        assertThat(julius.getSpiesOn().getTarget().getName()).isEqualTo(mata.getName());
        assertThat(julius.getSpiedOnBy().getCode()).isEqualTo("Hawk");
        assertThat(julius.getSpiedOnBy().getSpy().getName()).isEqualTo(mata.getName());
    }
}
