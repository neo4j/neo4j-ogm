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
package org.neo4j.ogm.persistence.model;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.gh791.EntityWithNativeByteArrays;
import org.neo4j.ogm.domain.gh791.EntityWithNativeByteArrays.SomeTuple;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

public class ArraysMappingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.social", "org.neo4j.ogm.domain.gh791");
    }

    @Before
    public void setUpMapper() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldGenerateCypherToPersistArraysOfPrimitives() {
        Individual individual = new Individual();
        individual.setName("Jeff");
        individual.setAge(41);
        individual.setBankBalance(1000.50f);
        individual.setPrimitiveIntArray(new int[] { 1, 6, 4, 7, 2 });

        session.save(individual);

        session.clear();
        assertThat(session.query(
            "MATCH (i:Individual {name:'Jeff', age:41, bankBalance: 1000.50, code:0, primitiveIntArray:[1,6,4,7,2]}) "
                + "return i", emptyMap()).queryResults()).hasSize(1);

        session.clear();

        Individual loadedIndividual = session.load(Individual.class, individual.getId());
        assertThat(loadedIndividual.getPrimitiveIntArray()).isEqualTo(individual.getPrimitiveIntArray());
    }

    @Test
    public void shouldGenerateCypherToPersistByteArray() {
        Individual individual = new Individual();
        individual.setAge(41);
        individual.setBankBalance(1000.50f);
        individual.setPrimitiveByteArray(new byte[] { 1, 2, 3, 4, 5 });

        session.save(individual);

        session.clear();
        assertThat(session.query(
            "MATCH (i:Individual {age:41, bankBalance: 1000.50, code:0, primitiveByteArray:'AQIDBAU='}) return i",
            emptyMap()).queryResults()).hasSize(1);

        session.clear();
        Iterable<Map<String, Object>> executionResult = session
            .query("MATCH (i:Individual) RETURN i.primitiveByteArray AS bytes",
                emptyMap()).queryResults();
        Map<String, Object> result = executionResult.iterator().next();
        assertThat(result.get("bytes")).as("The array wasn't persisted as the correct type")
            .isEqualTo("AQIDBAU="); //Byte arrays are converted to Base64 Strings
    }

    @Test
    public void shouldDeserializeByteArrays() {

        long id = (long) session.query(
            "CREATE (i:Individual {age:42, bankBalance: 23, code:6, primitiveByteArray:'AQIDBAU='}) return id(i) as id",
            Collections.emptyMap()).queryResults().iterator().next().get("id");

        session.clear();

        Session freshSession = sessionFactory.openSession();
        Individual loadedIndividual = freshSession.load(Individual.class, id);
        assertThat(loadedIndividual.getPrimitiveByteArray()).isEqualTo(new byte[] { 1, 2, 3, 4, 5 });
    }

    @Test // GH-791
    public void shouldDeserializeNativeByteArrays() {

        byte[] primitive = { 1, 2, 3, 4, 5 };
        Byte[] wrapped = { 9, 8, 7, 6, 5 };
        byte[] helloWorld = { 104, 101, 108, 108, 111, 64, 119, 111, 114, 108, 100 };

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("primitive", primitive);
        parameters.put("wrapped", wrapped);
        parameters.put("someTuple", helloWorld);
        long id = (long) session.query(
            "CREATE (i:EntityWithNativeByteArrays {primitive: $primitive, wrapped: $wrapped, someTuple: $someTuple}) return id(i) as id",
            parameters).queryResults().iterator().next()
            .get("id");

        session.clear();

        Session freshSession = sessionFactory.openSession();
        EntityWithNativeByteArrays loadedIndividual = freshSession.load(EntityWithNativeByteArrays.class, id);
        assertThat(loadedIndividual.getPrimitive()).isEqualTo(primitive);
        assertThat(loadedIndividual.getWrapped()).isEqualTo(wrapped);
        assertThat(loadedIndividual.getSomeTuple()).isEqualTo(new SomeTuple("hello", "world"));
    }

    @Test // GH-791
    public void shouldSerializeNativeByteArrays() {

        byte[] primitive = { 1, 2, 3, 4, 5 };
        Byte[] wrapped = { 9, 8, 7, 6, 5 };
        byte[] helloWorld = { 104, 101, 108, 108, 111, 64, 119, 111, 114, 108, 100 };

        EntityWithNativeByteArrays entityWithNativeByteArrays = new EntityWithNativeByteArrays();
        entityWithNativeByteArrays.setPrimitive(primitive);
        entityWithNativeByteArrays.setWrapped(wrapped);
        entityWithNativeByteArrays.setSomeTuple(new SomeTuple("hello", "world"));

        session.save(entityWithNativeByteArrays);

        Map<String, Object> result = session.query(
            "MATCH (i:EntityWithNativeByteArrays) WHERE id(i) = $id RETURN i.primitive AS primitiveArray, i.wrapped AS wrappedArray, i.someTuple as someTuple",
            singletonMap("id", entityWithNativeByteArrays.getId())).queryResults().iterator().next();

        byte[] primitiveArray = getBytes(result.get("primitiveArray"));
        byte[] wrappedArray = getBytes(result.get("wrappedArray"));
        String someTuple = new String(getBytes(result.get("someTuple")), StandardCharsets.UTF_8);

        assertThat(primitiveArray).isEqualTo(primitive);
        assertThat(wrappedArray).isEqualTo(wrapped);
        assertThat(someTuple).isEqualTo("hello@world");
    }

    private static byte[] getBytes(Object o) {
        if (o instanceof byte[]) {
            return (byte[]) o;
        } else if (o instanceof String) {
            return Base64.getDecoder().decode((String) o);
        } else {
            throw new IllegalArgumentException("Can only retrieve byte from byte[] or String.");
        }
    }

    @Test
    public void shouldGenerateCypherToPersistCollectionOfBoxedPrimitivesToArrayOfPrimitives() {
        Individual individual = new Individual();
        individual.setName("Gary");
        individual.setAge(36);
        individual.setBankBalance(99.25f);
        individual.setFavouriteRadioStations(new Vector<>(Arrays.asList(97.4, 105.4, 98.2)));

        session.save(individual);

        session.clear();
        assertThat(session.query(
            "MATCH (i:Individual {name:'Gary', age:36, bankBalance:99.25, code:0, favouriteRadioStations:[97.4, 105.4, 98.2]}) "
                + "return i", emptyMap()).queryResults()).hasSize(1);
    }

}
