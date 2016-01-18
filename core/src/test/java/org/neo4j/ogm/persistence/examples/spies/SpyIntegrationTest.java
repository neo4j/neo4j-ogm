/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.spies.Spy;
import org.neo4j.ogm.domain.spies.Target;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Luanne Misquitta
 */
public class SpyIntegrationTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.spies").openSession();
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
        assertNotNull(mata);
        assertEquals(julius.getName(), mata.getSpiesOn().getTarget().getName());
        assertEquals("Robin", mata.getSpiedOnBy().getCode());
        assertEquals(julius.getName(), mata.getSpiedOnBy().getSpy().getName());


        session.clear();
        julius = session.load(Spy.class, julius.getId());
        assertNotNull(julius);
        assertEquals(mata.getName(), julius.getSpiesOn().getTarget().getName());
        assertEquals("Hawk", julius.getSpiedOnBy().getCode());
        assertEquals(mata.getName(), julius.getSpiedOnBy().getSpy().getName());

    }


}
