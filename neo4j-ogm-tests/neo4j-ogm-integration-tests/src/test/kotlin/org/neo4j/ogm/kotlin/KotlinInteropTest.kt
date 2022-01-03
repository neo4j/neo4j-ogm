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
package org.neo4j.ogm.kotlin

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Values
import org.neo4j.harness.Neo4j
import org.neo4j.harness.Neo4jBuilders
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.config.ObjectMapperFactory
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.cypher.query.SortOrder
import org.neo4j.ogm.cypher.query.SortOrder.Direction
import org.neo4j.ogm.domain.dataclasses.MyNode
import org.neo4j.ogm.domain.dataclasses.OtherNode
import org.neo4j.ogm.domain.delegation.KotlinAImpl
import org.neo4j.ogm.domain.gh696.Lion
import org.neo4j.ogm.domain.gh696.Zebra
import org.neo4j.ogm.domain.gh696.ZooKotlin
import org.neo4j.ogm.domain.gh822.*
import org.neo4j.ogm.session.*
import kotlin.test.assertNotNull

/**
 * @author Michael J. Simons
 * @author Róbert Papp
 */
class KotlinInteropTest {

    companion object {
        @JvmStatic
        private lateinit var server: Neo4j

        private lateinit var driver: Driver

        private lateinit var sessionFactory: SessionFactory

        @BeforeClass
        @JvmStatic
        fun setup() {
            server = Neo4jBuilders.newInProcessBuilder().build()
            driver = GraphDatabase.driver(server.boltURI(), AuthTokens.none())

            val ogmConfiguration = Configuration.Builder()
                    .uri(server.boltURI().toString())
                    .build()
            sessionFactory = SessionFactory(ogmConfiguration,
                    MyNode::class.java.`package`.name,
                    KotlinAImpl::class.java.`package`.name,
                    ZooKotlin::class.java.`package`.name,
                    User::class.java.`package`.name
            )

            ObjectMapperFactory.objectMapper()
                    .registerModule(KotlinModule())
                    .registerModule(IdTypesModule())
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            sessionFactory.close()
            driver.close()
            server.close()
        }
    }

    private val names = listOf("Brian", "Roger", "John", "Freddie", "Farin", "Rod", "Bela")

    @Before
    fun prepareData() {

        driver.session().use {
            assertThat(it.run("CREATE (n:Unrelated)").consume().counters().nodesCreated()).isEqualTo(1)
            val summary = it.run("UNWIND \$names AS name CREATE (n:MyNode {name: name})", Values.parameters("names", names)).consume()
            assertThat(summary.counters().nodesCreated()).isEqualTo(names.size)

            assertThat(it.run("CREATE (n:A:Base)").consume().counters().nodesCreated()).isEqualTo(1)
            assertThat(it.run("CREATE (a1:Animal:Zebra) <- [:CONTAINS] - (z:ZooKotlin) - [:CONTAINS] -> (a2:Animal:Lion)")
                    .consume().counters().nodesCreated()).isEqualTo(3)
        }
    }

    @After
    fun purgeData() {

        driver.session().use { it.run("MATCH (n) DETACH DELETE n").consume() }
    }

    @Test
    fun basicMappingShouldWork() {

        val myNode = MyNode(name = "Node1", description = "A node", otherNodes = listOf(OtherNode(name = "o1"), OtherNode(name = "o2")))
        sessionFactory.openSession().save(myNode)

        val loadedNode: MyNode? = sessionFactory.openSession().load(myNode.dbId!!)
        assertNotNull(loadedNode)
        assertThat(loadedNode.name).isEqualTo("Node1")
        assertThat(loadedNode.description).isEqualTo("A node")
        assertThat(loadedNode.otherNodes)
                .hasSize(2)
                .extracting("name").containsExactlyInAnyOrder("o1", "o2")

        driver.session().use {
            val resultList = it.run("MATCH (n:MyNode) WHERE id(n) = \$id RETURN n", Values.parameters("id", myNode.dbId)).list()

            assertThat(resultList).hasSize(1)
            resultList.forEach { record ->
                assertThat(record.get("n").asMap()).containsKeys("name", "description")
            }
        }
    }

    @Test // GH-685
    fun `Implementation by delegate should work`() {

        val nodes = sessionFactory.openSession().loadAll<KotlinAImpl>()
        assertThat(nodes.map { it.baseName }).containsExactly("someValue")

        sessionFactory.openSession().save(KotlinAImpl())

        driver.session().use {
            val resultList = it.run("MATCH (n:A:Base) RETURN count(n) as n ").single()["n"].asLong()
            assertThat(resultList).isEqualTo(2L)
        }
    }

    @Test // GH-696
    fun `Kotlin based wildcard mapping should work`() {

        val nodes = sessionFactory.openSession().loadAll<ZooKotlin>()
        assertThat(nodes).hasSize(1)
        assertThat(nodes.first().animals).hasSize(2)
        assertThat(nodes.first().animals!!.map { it::class.java }).containsExactlyInAnyOrder(Lion::class.java, Zebra::class.java)
    }

    @Test
    fun `loadAll with ids should work`() {

        val ids: List<Long> = driver.session().use { session ->
            session.run("MATCH (n:MyNode) WHERE n.name IN ['Rod', 'John'] RETURN id(n) as id").list { it["id"].asLong() }
        }

        val nodes = sessionFactory.openSession().loadAll<MyNode>(ids, SortOrder(Direction.ASC, "name"))
        assertThat(nodes.map { it.name }).containsExactly("John", "Rod")
    }

    @Test
    fun `loadAll should work`() {

        val nodes = sessionFactory.openSession().loadAll<MyNode>(SortOrder(Direction.DESC, "name"))
        assertThat(nodes.map { it.name }).containsExactlyElementsOf(names.sortedDescending())
    }

    @Test
    fun `loadAll with filter should work`() {

        val nodes = sessionFactory.openSession().loadAll<MyNode>(Filter("name", ComparisonOperator.EQUALS, "John"))
        assertThat(nodes.map { it.name }).containsExactly("John")
    }

    @Test
    fun `loadAll with filters should work`() {

        val filters = Filters(Filter("name", ComparisonOperator.EQUALS, "Roger")).or(Filter("name", ComparisonOperator.EQUALS, "Bela"))
        val nodes = sessionFactory.openSession().loadAll<MyNode>(filters, SortOrder(Direction.ASC, "name"))
        assertThat(nodes.map { it.name }).containsExactly("Bela", "Roger")
    }

    @Test
    fun `deleteAll should work`() {

        sessionFactory.openSession().deleteAll<MyNode>()
        val remainingNumberOfNodes = driver.session().use {
            it.run("MATCH (n:Unrelated) RETURN count(n) AS cnt").single()["cnt"].asLong()
        }
        assertThat(remainingNumberOfNodes).isEqualTo(1L)
    }

    @Test
    fun `delete with filter should work`() {

        val deletedObjects : Any = sessionFactory.openSession().delete<MyNode>(listOf(Filter("name", ComparisonOperator.EQUALS, "Freddie")), false)

        assertThat(deletedObjects).isEqualTo(1L)
    }

    @Test
    fun `queryForObject should work`() {

        val farin : MyNode? = sessionFactory.openSession().queryForObject("MATCH (n:MyNode {name: \$name}) RETURN n", mapOf(Pair("name", "Farin")))
        assertNotNull(farin)
        assertThat(farin.name).isEqualTo("Farin")
    }

    @Test
    fun `query should work`() {

        val returnedNames : Iterable<String> = sessionFactory.openSession().query("MATCH (n:MyNode) RETURN n.name ORDER BY n.name DESC")
        assertThat(returnedNames).containsExactlyElementsOf(names.sortedDescending())
    }

    @Test
    fun `countEntitiesOfType should work`() {

        val numberOfMyNodes = sessionFactory.openSession().countEntitiesOfType<MyNode>()
        assertThat(numberOfMyNodes).isEqualTo(names.size.toLong())
    }

    @Test
    fun `count should work`() {

        val numberOfMyNodes = sessionFactory.openSession().count<MyNode>(listOf(Filter("name", ComparisonOperator.EQUALS, "Brian")))
        assertThat(numberOfMyNodes).isEqualTo(1)
    }

    @Test // GH-822
    fun `inline classes should work with dedicated serializers`() {

        val userId = StringID("aUserId")
        val userToSave = User(userId, "Danger Dan");

        sessionFactory.openSession().save(userToSave);

        val nameFilter = Filter("userId", ComparisonOperator.EQUALS, userId).ignoreCase()
        val loadedUsers = sessionFactory.openSession().loadAll(User::class.java, nameFilter);
        assertThat(loadedUsers).hasSize(1).extracting<StringID>(User::userId).containsOnly(userId)

        val loadedUser = sessionFactory.openSession().queryForObject<User>("MATCH (u:User) RETURN u", mapOf(Pair("userId", userId)))!!
        assertThat(loadedUser).isNotNull().extracting(User::userId).isEqualTo(userId)
    }
}
