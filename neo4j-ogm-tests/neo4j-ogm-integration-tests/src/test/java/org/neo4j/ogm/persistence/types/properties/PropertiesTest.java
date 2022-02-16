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
package org.neo4j.ogm.persistence.types.properties;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.ogm.domain.properties.SomeNode;
import org.neo4j.ogm.domain.properties.User;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class PropertiesTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @Rule
    public ExpectedException thrownException = ExpectedException.none();

    @BeforeClass
    public static void init() {
        sessionFactory = new SessionFactory(getDriver(), User.class.getName(), SomeNode.class.getName());
    }

    @Before
    public void setUp() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test // GH-632
    public void shouldHandleEnumsAsKey() {

        User user = new User("A");
        user.setEnumAProperties(Collections.singletonMap(User.EnumA.VALUE_AA, "aa"));
        user.setEnumBProperties(Collections.singletonMap(User.EnumB.VALUE_BA, "ba"));

        session.save(user);

        session.clear();

        user = session.load(User.class, user.getId());
        assertThat(user.getEnumAProperties()).containsEntry(User.EnumA.VALUE_AA, "aa");
        assertThat(user.getEnumBProperties()).containsEntry(User.EnumB.VALUE_BA, "ba");

        session.clear();
        User loadedObject = (User) session.query(
                "MATCH (n) where id(n)= $id return n", Collections.singletonMap("id", user.getId()))
            .queryResults().iterator().next()
            .get("n");
        assertThat(loadedObject.getEnumAProperties()).containsEntry(User.EnumA.VALUE_AA, "aa");
        assertThat(loadedObject.getEnumBProperties()).containsEntry(User.EnumB.VALUE_BA, "ba");
    }

    @Test // GH-899
    public void shouldHandleEnumsAsValues() {

        User user = new User("A");
        user.setEnumAValuesByString(Collections.singletonMap("aa", User.EnumA.VALUE_AA));
        user.setEnumBValuesByEnum(Collections.singletonMap(User.EnumA.VALUE_AA, User.EnumB.VALUE_BA));

        session.save(user);

        session.clear();

        user = session.load(User.class, user.getId());
        assertThat(user.getEnumAValuesByString()).containsEntry("aa", User.EnumA.VALUE_AA);
        assertThat(user.getEnumBValuesByEnum()).containsEntry(User.EnumA.VALUE_AA, User.EnumB.VALUE_BA);

        session.clear();
        User loadedObject = (User) session.query(
                "MATCH (n) where id(n)= $id AND n.`enumAValuesByString.aa` = 'VALUE_AA' and n.`enumBValuesByEnum.VALUE_AA` = 'VALUE_BA' return n", Collections.singletonMap("id", user.getId()))
            .queryResults().iterator().next()
            .get("n");
        assertThat(loadedObject.getEnumAValuesByString()).containsEntry("aa", User.EnumA.VALUE_AA);
        assertThat(loadedObject.getEnumBValuesByEnum()).containsEntry(User.EnumA.VALUE_AA, User.EnumB.VALUE_BA);
    }

    @Test // GH-634
    public void shouldHandleFilteredProperties() {

        User user = new User("A");
        user.setFilteredProperties(Collections.singletonMap(User.EnumA.VALUE_AA, "aa"));

        session.save(user);
        session.clear();

        user = session.load(User.class, user.getId());
        assertThat(user.getFilteredProperties()).containsEntry(User.EnumA.VALUE_AA, "aa");

        session.clear();
        User loadedObject = (User) session.query(
            "MATCH (n) where id(n)=" + user.getId() + " return n", emptyMap())
            .queryResults().iterator().next()
            .get("n");
        assertThat(loadedObject.getFilteredProperties()).containsEntry(User.EnumA.VALUE_AA, "aa");
    }

    @Test // GH-632
    public void shouldNotAllowNullKeys() {

        thrownException.expect(UnsupportedOperationException.class);
        thrownException.expectMessage("Null is not a supported property key!");

        User user = new User("A");
        Map<String, Object> properties = new HashMap<>();
        properties.put(null, "irrelevant");
        user.setMyProperties(properties);
        session.save(user);

        session.clear();
    }

    @Test // GH-632
    public void shouldNotAllowKeysOtherThanStringAndEnum() {

        thrownException.expect(UnsupportedOperationException.class);
        thrownException.expectMessage("Only String and Enum allowed to be keys, got class java.lang.Integer");

        User user = new User("A");
        Map properties = new HashMap<>();
        properties.put(123, "irrelevant");
        user.setMyProperties(properties);
        session.save(user);

        session.clear();
    }

    @Test
    public void shouldMapPropertiesAttributeToNodeProperties() {
        User user = new User("Frantisek");
        user.putMyProperty("city", "London");
        user.putMyProperty("zipCode", "SW1A 1AA");

        session.save(user);
        session.clear();
        User loadedObject = (User) session.query(
            "MATCH (n) where id(n)=" + user.getId() + " return n", emptyMap())
            .queryResults().iterator().next()
            .get("n");
        assertThat(loadedObject.getMyProperties()).containsEntry("city", "London");
        assertThat(loadedObject.getMyProperties()).containsEntry("zipCode", "SW1A 1AA");
    }

    @Test
    public void shouldMapPropertiesAttributeWithNestedMapToNodeProperties() {
        User user = new User("Frantisek");
        Map<String, String> address = new HashMap<String, String>();
        address.put("city", "London");
        address.put("zipCode", "SW1A 1AA");
        user.putMyProperty("address", address);

        session.save(user);
        session.clear();
        User loadedObject = (User) session.query(
            "MATCH (n) where id(n)=" + user.getId() + " return n", emptyMap())
            .queryResults().iterator().next()
            .get("n");
        assertThat(loadedObject.getMyProperties()).containsEntry("address", address);
    }

    @Test
    public void shouldMapPropertiesAttributeWithPrefixToNodeProperties() {
        User user = new User("Frantisek");
        user.putPrefixedProperty("city", "London");
        user.putPrefixedProperty("zipCode", "SW1A 1AA");

        session.save(user);
        session.clear();
        User loadedObject = (User) session.query(
            "MATCH (n) where id(n)=" + user.getId() + " return n", emptyMap())
            .queryResults().iterator().next()
            .get("n");
        assertThat(loadedObject.getPrefixedProperties()).containsEntry("city", "London");
        assertThat(loadedObject.getPrefixedProperties()).containsEntry("zipCode", "SW1A 1AA");
    }

    @Test
    public void shouldMapPropertiesAttributeWithDelimiterToNodeProperties() {
        User user = new User("Frantisek");
        user.putDelimiterProperty("city", "London");
        user.putDelimiterProperty("zipCode", "SW1A 1AA");

        session.save(user);
        session.clear();
        User loadedObject = (User) session.query(
            "MATCH (n) where id(n)=" + user.getId() + " return n", emptyMap())
            .queryResults().iterator().next()
            .get("n");
        assertThat(loadedObject.getDelimiterProperties()).containsEntry("city", "London");
        assertThat(loadedObject.getDelimiterProperties()).containsEntry("zipCode", "SW1A 1AA");
    }

    @Test
    public void shouldMapNodePropertiesToPropertiesAttribute() {
        session.query("CREATE (u:User {`name`:'Frantisek', `myProperties.city`:'London', " +
                "`myProperties.zipCode`:'SW1A 1AA'})",
            emptyMap());

        User user = session.loadAll(User.class).iterator().next();
        assertThat(user.getMyProperties())
            .hasSize(2)
            .doesNotContainKey("name")
            .containsEntry("city", "London")
            .containsEntry("zipCode", "SW1A 1AA");
    }

    @Test
    public void shouldMapNestedNodePropertiesToPropertiesAttributeAsNestedMap() {
        session.query("CREATE (u:User {`name`:'Frantisek', " +
                "`myProperties.address.city`:'London', " +
                "`myProperties.address.zipCode`:'SW1A 1AA'})",
            emptyMap());

        User user = session.loadAll(User.class).iterator().next();
        Map<String, Object> address = (Map<String, Object>) user.getMyProperties().get("address");
        assertThat(address)
            .hasSize(2)
            .containsEntry("city", "London")
            .containsEntry("zipCode", "SW1A 1AA");
    }

    @Test
    public void shouldMapNodePropertiesToPropertiesAttributeWithPrefix() {
        session.query("CREATE (u:User {`name`:'Frantisek', `myPrefix.city`:'London', `myPrefix.zipCode`:'SW1A 1AA'})",
            emptyMap());

        User user = session.loadAll(User.class).iterator().next();
        assertThat(user.getPrefixedProperties())
            .hasSize(2)
            .doesNotContainKey("name")
            .containsEntry("city", "London")
            .containsEntry("zipCode", "SW1A 1AA");
    }

    @Test
    public void shouldMapNodePropertiesToPropertiesAttributeWithDelimiter() {
        session.query("CREATE (u:User {`name`:'Frantisek', " +
                "`delimiterProperties__city`:'London', " +
                "`delimiterProperties__zipCode`:'SW1A 1AA'})",
            emptyMap());

        User user = session.loadAll(User.class).iterator().next();
        assertThat(user.getDelimiterProperties())
            .hasSize(2)
            .doesNotContainKey("name")
            .containsEntry("city", "London")
            .containsEntry("zipCode", "SW1A 1AA");
    }

    @Test
    public void shouldSaveAndLoadMapOfAllPropertyTypes() {
        //        propertyMap.put("Character", 'c');
        //        propertyMap.put("Byte", (byte) 2);
        //        propertyMap.put("Short", (short) 3);
        //        propertyMap.put("Integer", 4);
        //        propertyMap.put("Float", 6.0f);
        //        propertyMap.put("Character[]", new Character[]{'c', 'h', 'a', 'r'});
        //        propertyMap.put("Byte[]", new Byte[]{2, 3, 4});
        //        propertyMap.put("Short[]", new Short[]{3, 4, 5});
        //        propertyMap.put("Integer[]", new Integer[]{4, 5, 6});
        //        propertyMap.put("Float[]", new Float[]{6.0f, 7.0f, 8.0f});
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("Long", 5L);
        propertyMap.put("Double", 7.0d);
        propertyMap.put("Boolean", true);
        propertyMap.put("String", "String");
        propertyMap.put("Long[]", Arrays.asList(5L, 6L, 7L));
        propertyMap.put("Double[]", Arrays.asList(7.0d, 8.0d, 9.0d));
        propertyMap.put("Boolean[]", Arrays.asList(true, false, true));
        propertyMap.put("String[]", Arrays.asList("S", "t", "r", "i", "n", "g"));

        User user = new User();
        user.setMyProperties(propertyMap);

        session.save(user);
        session.clear();

        User loaded = session.load(User.class, user.getId());
        assertThat(loaded.getMyProperties()).isEqualTo(propertyMap);
    }

    @Test(expected = MappingException.class)
    public void shouldThrowExceptionWhenMappingNonCypherType() {

        User user = new User();
        user.putMyProperty("age", 18);

        session.save(user);
    }

    @Test(expected = MappingException.class)
    public void shouldThrowExceptionWhenMappingNonConvertibleType() {

        User user = new User();
        user.putAllowCastProperty("age", new Date());

        session.save(user);
    }

    @Test
    public void shouldMapSpecificValueType() {

        User user = new User();
        user.putIntegerProperty("age", 18);

        session.save(user);
        session.clear();

        User loaded = session.load(User.class, user.getId());

        assertThat(loaded.getIntegerProperties()).containsEntry("age", 18);
    }

    @Test
    public void shouldConvertNestedMapWithList() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("value", Arrays.asList(1, 2, 3, 4));

        User user = new User();
        user.putMyProperty("nested", nested);

        session.save(user);
        session.clear();

        User loaded = session.load(User.class, user.getId());
        assertThat(loaded.getMyProperties()).isEqualTo(loaded.getMyProperties());
    }

    @Test // GH-518
    public void shouldBeAbleToDeletePropertiesAgain() {
        User user = new User();

        user.putMyProperty("prop1", "A property");
        user.putMyProperty("prop2", "Another property");

        user.putIntegerProperty("anInt", 1);
        user.putIntegerProperty("anotherInt", 2);

        user.putDelimiterProperty("a", "b");

        session.save(user);
        session.clear();

        User loaded = session.load(User.class, user.getId());
        assertThat(loaded.getMyProperties()).containsKeys("prop1", "prop2");
        assertThat(loaded.getIntegerProperties()).hasSize(2);
        assertThat(loaded.getDelimiterProperties()).hasSize(1);
        assertThat(loaded.getPrefixedProperties()).isEmpty();

        loaded.getMyProperties().remove("prop1");
        loaded.getIntegerProperties().clear();
        loaded.setDelimiterProperties(null);

        session.save(loaded);
        session.clear();

        loaded = session.load(User.class, user.getId());
        assertThat(loaded.getMyProperties()).containsKeys("prop2");
        assertThat(loaded.getIntegerProperties()).isEmpty();
        assertThat(loaded.getDelimiterProperties()).isEmpty();
        assertThat(loaded.getPrefixedProperties()).isEmpty();
    }

    @Test  // GH-518
    public void shouldNotDeleteUnmappedProperties() {
        session.query("CREATE (u:SomeNode {`name`:'Unmapped', `myPrefix.aProperty`:'aValue'})",
            emptyMap());

        // Make sure all the mapped properties are there
        SomeNode someNode = session.loadAll(SomeNode.class).iterator().next();
        assertThat(someNode.getPrefixedProperties())
            .hasSize(1)
            .containsEntry("aProperty", "aValue");

        // Then remove some
        someNode.getPrefixedProperties().remove("aProperty");
        someNode.getPrefixedProperties().put("anotherProperty", "anotherValue");
        session.save(someNode);

        // Check if the unmapped is still there
        session.clear();
        Iterator<Map<String, Object>> result = session
            .query(
                "MATCH (u:SomeNode) WHERE id(u) = $id RETURN u.name as name, u.`myPrefix.anotherProperty` as anotherProperty",
                Collections.singletonMap("id", someNode.getId())).iterator();

        assertThat(result.hasNext()).isTrue();
        result.forEachRemaining(m ->
            assertThat(m)
                .hasSize(2)
                .containsEntry("name", "Unmapped")
                .containsEntry("anotherProperty", "anotherValue")
        );
    }

    @Test // GH-650
    public void manualConversionShouldSupportPropertiesWithouthPrefix() {
        User user = new User();
        Map<String, Object> properties = new HashMap<>();
        properties.put("a", 1L);
        properties.put("b", 2L);

        user.setManualProperties(properties);

        user.putMyProperty("prop1", "A property");
        user.putMyProperty("prop2", "Another property");

        session.save(user);
        session.clear();
        user = session.load(User.class, user.getId());
        assertThat(user.getManualProperties())
            .containsOnly(new HashMap.SimpleEntry<>("a", 1L), new HashMap.SimpleEntry<>("b", 2L));

        assertThat(user.getMyProperties())
            .containsOnlyKeys("prop1", "prop2");
    }
}
