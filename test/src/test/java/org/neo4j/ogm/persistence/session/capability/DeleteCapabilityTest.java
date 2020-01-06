/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author vince
 */
public class DeleteCapabilityTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.music");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldNotFailIfDeleteNodeEntityAgainstEmptyDatabase() {
        session.deleteAll(Album.class);
    }

    @Test
    public void shouldNotFailIfDeleteRelationshipEntityAgainstEmptyDatabase() {
        session.deleteAll(Recording.class);
    }
}
