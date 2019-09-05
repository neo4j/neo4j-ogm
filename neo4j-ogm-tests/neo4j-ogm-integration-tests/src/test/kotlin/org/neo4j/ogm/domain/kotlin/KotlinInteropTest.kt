/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.domain.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Values
import org.neo4j.harness.ServerControls
import org.neo4j.harness.TestServerBuilders
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.domain.dataclasses.MyNode
import org.neo4j.ogm.domain.dataclasses.OtherNode
import org.neo4j.ogm.session.SessionFactory

/**
 * @author Michael J. Simons
 */
class KotlinInteropTest {

    companion object {
        @JvmStatic
        private lateinit var server: ServerControls

        @BeforeClass
        @JvmStatic
        fun setup() {
            server = TestServerBuilders.newInProcessBuilder().newServer()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            server.close()
        }
    }

    @Test
    fun basicMappingShouldWork() {
        val ogmConfiguration = Configuration.Builder()
                .uri(server.boltURI().toString())
                .build()

        val sessionFactory = SessionFactory(ogmConfiguration, MyNode::class.java.`package`.name)
        var myNode: MyNode

        try {
            myNode = MyNode(name = "Node1", description = "A node", otherNodes = listOf(OtherNode(name = "o1"), OtherNode(name = "o2")))
            sessionFactory.openSession().save(myNode)

            myNode = sessionFactory.openSession().load(MyNode::class.java, myNode.dbId)
            assertThat(myNode.name).isEqualTo("Node1")
            assertThat(myNode.description).isEqualTo("A node")
            assertThat(myNode.otherNodes)
                    .hasSize(2)
                    .extracting("name").containsExactlyInAnyOrder("o1", "o2")
        } finally {
            sessionFactory.close()
        }

        GraphDatabase.driver(server.boltURI(), AuthTokens.none()).use { driver ->
            driver.session().use { session ->
                val resultList = session.run("MATCH (n:MyNode) WHERE id(n) = \$id RETURN n", Values.parameters("id", myNode.dbId)).list()

                assertThat(resultList).hasSize(1)
                resultList.forEach { record ->
                    assertThat(record.get("n").asMap()).containsKeys("name", "description")
                }
            }
        }
    }
}
