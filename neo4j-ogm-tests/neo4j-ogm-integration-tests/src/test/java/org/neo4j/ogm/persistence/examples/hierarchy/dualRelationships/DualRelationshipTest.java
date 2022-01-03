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
package org.neo4j.ogm.persistence.examples.hierarchy.dualRelationships;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.hierarchy.dualRelation.DataView;
import org.neo4j.ogm.domain.hierarchy.dualRelation.DataViewOwned;
import org.neo4j.ogm.domain.hierarchy.dualRelation.Thing;
import org.neo4j.ogm.domain.hierarchy.dualRelation.ThingOwned;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * This test passes because DataView and DataViewOwned have their setSharedWith annotated with @Relationship.
 * If the setter were not annotated, these tests would fail.
 *
 * GH-144
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class DualRelationshipTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.hierarchy.dualRelation");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    @After
    public void cleanup() {
        session.purgeDatabase();
    }

    @Test
    public void shouldRehydrateProperlyUsingLoad() {

        Thing thing1 = new Thing();
        Thing thing2 = new Thing();
        DataView dataview = new DataView();
        dataview.setName("dataview");

        thing1.setName("owner");
        thing2.setName("shared");

        dataview.setOwner(thing1);
        dataview.getSharedWith().add(thing2);

        session.save(dataview);

        session.clear();

        DataView found = session.load(DataView.class, dataview.getId());

        assertThat(found.getOwner().getName()).isEqualTo("owner");
        assertThat(found.getSharedWith().get(0).getName()).isEqualTo("shared");
        assertThat(found.getSharedWith()).hasSize(1);
    }

    @Test
    public void shouldRehydrateProperlyWithQuery() {

        Thing thing1 = new Thing();
        Thing thing2 = new Thing();
        DataView dataview = new DataView();
        dataview.setName("dataview");

        thing1.setName("owner");
        thing2.setName("shared");

        dataview.setOwner(thing1);
        dataview.getSharedWith().add(thing2);

        session.save(dataview);

        session.clear();

        String query = "MATCH (n:DataView) WITH n MATCH p=(n)-[*0..1]-(m) RETURN n,nodes(p),relationships(p)";

        DataView found = session.queryForObject(DataView.class, query, Collections.emptyMap());

        assertThat(found.getSharedWith()).hasSize(1);
        assertThat(found.getOwner().getName()).isEqualTo("owner");
        assertThat(found.getSharedWith().get(0).getName()).isEqualTo("shared");
    }

    @Test
    public void shouldRehydrateEntitiesWithAbstractParentProperlyUsingLoad() {

        ThingOwned thing1 = new ThingOwned();
        ThingOwned thing2 = new ThingOwned();
        DataViewOwned dataview = new DataViewOwned();
        dataview.setName("dataview");

        thing1.setName("owner");
        thing2.setName("shared");

        dataview.setOwner(thing1);
        dataview.getSharedWith().add(thing2);

        session.save(dataview);

        session.clear();

        DataViewOwned found = session.load(DataViewOwned.class, dataview.getId());

        assertThat(found.getOwner().getName()).isEqualTo("owner");
        assertThat(found.getSharedWith().get(0).getName()).isEqualTo("shared");
        assertThat(found.getSharedWith()).hasSize(1);
    }

    @Test
    public void shouldRehydrateEntitiesWithAbstractParentProperlyWithQuery() {

        ThingOwned thing1 = new ThingOwned();
        ThingOwned thing2 = new ThingOwned();
        DataViewOwned dataview = new DataViewOwned();
        dataview.setName("dataview");

        thing1.setName("owner");
        thing2.setName("shared");

        dataview.setOwner(thing1);
        dataview.getSharedWith().add(thing2);

        session.save(dataview);

        session.clear();

        String query = "MATCH (n:DataViewOwned) WITH n MATCH p=(n)-[*0..1]-(m) RETURN n,nodes(p),relationships(p)";

        DataViewOwned found = session.queryForObject(DataViewOwned.class, query, Collections.emptyMap());

        assertThat(found).isNotNull();
        assertThat(found.getSharedWith()).hasSize(1);
        assertThat(found.getOwner().getName()).isEqualTo("owner");
        assertThat(found.getSharedWith().get(0).getName()).isEqualTo("shared");
    }
}
