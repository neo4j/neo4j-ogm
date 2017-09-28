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

package org.neo4j.ogm.persistence.examples.tree;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import org.neo4j.ogm.domain.tree.Node;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class SingleDirectionMappingTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.tree").openSession();
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

