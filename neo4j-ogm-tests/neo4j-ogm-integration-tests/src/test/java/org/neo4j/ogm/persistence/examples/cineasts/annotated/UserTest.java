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
        Neo4jSession session = new Neo4jSession(metadata, true, new UsersRequest());

        User user = session.load(User.class, "luanne", 1);

        assertThat(user.getLogin()).isEqualTo("luanne");
        assertThat(user.getSecurityRoles()).isNotNull();
        assertThat(user.getSecurityRoles().length).isEqualTo(2);
    }
}
