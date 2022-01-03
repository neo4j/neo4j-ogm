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
package org.neo4j.ogm.persistence.examples.convertible;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.convertible.parametrized.JsonNode;
import org.neo4j.ogm.domain.convertible.parametrized.StringMapEntity;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public class ParameterizedConversionTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.convertible.parametrized");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }

    @Test
    public void shouldConvertParametrizedMap() {

        JsonNode jsonNode = new JsonNode();
        jsonNode.payload = Collections.singletonMap("key", "value");

        session.save(jsonNode);

        session.clear();

        JsonNode found = session.load(JsonNode.class, jsonNode.id);

        assertThat(found.payload.containsKey("key")).isTrue();
        assertThat(found.payload.get("key")).isEqualTo("value");
    }

    @Test // GH-102
    public void shouldConvertParameterizedStringMap() {
        StringMapEntity entity = new StringMapEntity();
        session.save(entity);

        session.clear();

        StringMapEntity loaded = session.load(StringMapEntity.class, entity.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getStringMap()).hasSize(3);
    }
}
