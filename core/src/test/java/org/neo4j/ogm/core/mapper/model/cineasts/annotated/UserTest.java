/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.core.mapper.model.cineasts.annotated;

import org.junit.Test;
import org.neo4j.ogm.core.domain.cineasts.annotated.User;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author Luanne Misquitta
 */
public class UserTest {

    @Test
    public void testDeserialiseUserWithArrayOfEnums() {

        UsersRequest userRequest = new UsersRequest();

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
        Neo4jSession session = ((Neo4jSession) sessionFactory.openSession("dummy-url"));
        session.setDriver(userRequest);

        User user = session.load(User.class, 15L, 1);

        assertEquals("luanne", user.getLogin());
        assertNotNull(user.getSecurityRoles());
        assertEquals(2, user.getSecurityRoles().length);
    }
}
