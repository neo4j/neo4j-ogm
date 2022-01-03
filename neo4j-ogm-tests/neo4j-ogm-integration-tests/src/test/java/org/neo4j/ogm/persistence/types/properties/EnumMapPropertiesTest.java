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
package org.neo4j.ogm.persistence.types.properties;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.neo4j.ogm.domain.properties.UserWithEnumMap.UserProperties.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.properties.UserWithEnumMap;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class EnumMapPropertiesTest extends TestContainersTestBase {

    private static Session session;

    @BeforeClass
    public static void init() {
        session = new SessionFactory(getDriver(),
            UserWithEnumMap.class.getName())
            .openSession();
    }

    @Before
    public void setUp() {
        session.purgeDatabase();
    }

    @Test
    public void shouldMapMapAttributeToProperties() {
        UserWithEnumMap user = new UserWithEnumMap("Frantisek");
        user.putMyProperty(CITY, "London");
        user.putMyProperty(ZIP_CODE, "SW1A 1AA");

        session.save(user);
        session.clear();
        UserWithEnumMap loadedObject = (UserWithEnumMap) session.query(
            "MATCH (n) where id(n)=" + user.getId() + " return n", emptyMap())
            .queryResults().iterator().next()
            .get("n");

        assertThat(loadedObject.getMyProperties())
            .hasSize(2)
            .containsEntry(CITY, "London")
            .containsEntry(ZIP_CODE, "SW1A 1AA");

    }

    @Test
    public void shouldMapNodePropertiesToPropertiesAttribute() {
        session.query(
            "CREATE (u:User {`name`:'Frantisek', `myProperties.CITY`:'London', `myProperties.ZIP_CODE`:'SW1A 1AA'})",
            emptyMap());

        UserWithEnumMap user = session.loadAll(UserWithEnumMap.class).iterator().next();
        assertThat(user.getMyProperties())
            .hasSize(2)
            .containsEntry(CITY, "London")
            .containsEntry(ZIP_CODE, "SW1A 1AA");
    }

}
