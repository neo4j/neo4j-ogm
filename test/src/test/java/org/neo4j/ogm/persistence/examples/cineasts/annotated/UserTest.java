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

package org.neo4j.ogm.persistence.examples.cineasts.annotated;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.Neo4jSession;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class UserTest {

    @Test
    public void testDeserialiseUserWithArrayOfEnums() {

        MetaData metadata = new MetaData("org.neo4j.ogm.domain.cineasts.annotated");
        Neo4jSession session = new Neo4jSession(metadata, new UsersRequest());

        User user = session.load(User.class, "luanne", 1);

        assertThat(user.getLogin()).isEqualTo("luanne");
        assertThat(user.getSecurityRoles()).isNotNull();
        assertThat(user.getSecurityRoles().length).isEqualTo(2);
    }
}
