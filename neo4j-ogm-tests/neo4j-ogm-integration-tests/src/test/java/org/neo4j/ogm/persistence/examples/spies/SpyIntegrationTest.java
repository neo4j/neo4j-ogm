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
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 */
public class SpyIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.spies");
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
