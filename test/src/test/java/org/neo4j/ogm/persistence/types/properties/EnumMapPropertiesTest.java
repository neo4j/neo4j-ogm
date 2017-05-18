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

package org.neo4j.ogm.persistence.types.properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.domain.properties.UserWithEnumMap;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.ogm.domain.properties.UserWithEnumMap.UserProperties.CITY;
import static org.neo4j.ogm.domain.properties.UserWithEnumMap.UserProperties.ZIP_CODE;
import static org.neo4j.ogm.testutil.GraphTestUtils.assertSameGraph;

/**
 * @author Frantisek Hartman
 */
public class EnumMapPropertiesTest extends MultiDriverTestClass {

    private static Session session;

    @BeforeClass
    public static void init() throws IOException {
        session = new SessionFactory(getBaseConfiguration().build(),
                UserWithEnumMap.class.getName())
                .openSession();
    }

    @Before
    public void setUp() throws Exception {
        session.purgeDatabase();
    }

    @Test
    public void shouldMapMapAttributeToProperties() throws Exception {
        UserWithEnumMap user = new UserWithEnumMap("Frantisek");
        user.putMyProperty(CITY, "London");
        user.putMyProperty(ZIP_CODE, "SW1A 1AA");

        session.save(user);

        try (Transaction tx = getGraphDatabaseService().beginTx()) {
            Node userNode = getGraphDatabaseService().getNodeById(user.getId());
            assertThat(userNode.getAllProperties())
                    .hasSize(3)
                    .containsEntry("name", "Frantisek")
                    .containsEntry("myProperties.CITY", "London")
                    .containsEntry("myProperties.ZIP_CODE", "SW1A 1AA");

            tx.success();
        }
    }

    @Test
    public void shouldMapNodePropertiesToPropertiesAttribute() throws Exception {
        session.query("CREATE (u:User {`name`:'Frantisek', `myProperties.CITY`:'London', `myProperties.ZIP_CODE`:'SW1A 1AA'})",
                emptyMap());

        UserWithEnumMap user = session.loadAll(UserWithEnumMap.class).iterator().next();
        assertThat(user.getMyProperties())
                .hasSize(2)
                .containsEntry(CITY, "London")
                .containsEntry(ZIP_CODE, "SW1A 1AA");
    }

}
