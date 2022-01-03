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
package org.neo4j.ogm.persistence.examples.tree;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.tree.Node;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 *
 */
public class SingleDirectionMappingTest extends TestContainersTestBase {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.tree").openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldLoadCorrectRelationshipsWhenUsingDefaultDirection() throws Exception {
        Node node1 = new Node("Node 1");
        Node node2 = new Node("Node 2");
        node1.add(node2);
        Node node3 = new Node("Node 3");
        node2.add(node3);

        session.save(node1);

        Node loaded = session.load(Node.class, node1.getId());

        assertThat(loaded.getNodes()).hasSize(1);
        assertThat(loaded.getNodes().iterator().next().getNodes()).hasSize(1);
    }
}

