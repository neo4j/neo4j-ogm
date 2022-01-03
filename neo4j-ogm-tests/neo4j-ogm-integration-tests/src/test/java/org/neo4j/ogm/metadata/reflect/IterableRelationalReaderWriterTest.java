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
package org.neo4j.ogm.metadata.reflect;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.entityMapping.iterables.*;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.FieldInfo;

/**
 * @author Luanne Misquitta
 */
public class IterableRelationalReaderWriterTest {

    final String KNOWS = "KNOWS";
    final String LIKES = "LIKES";
    private DomainInfo domainInfo;

    @Before
    public void setup() {
        domainInfo = DomainInfo.create("org.neo4j.ogm.domain.entityMapping.iterables");
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV1() {
        final String KNOWN_BY = "KNOWN_BY";

        ClassInfo classInfo = this.domainInfo.getClass(UserV1.class.getName());
        UserV1 instance = new UserV1();
        Set<UserV1> relatedObject = Collections.singleton(new UserV1());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV1.class, KNOWN_BY, Relationship.Direction.OUTGOING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV1.class, KNOWN_BY, Relationship.Direction.INCOMING);

        relationalReader.write(instance, relatedObject);
        assertThat(instance.getKnownBy()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV2() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV2.class.getName());
        UserV2 instance = new UserV2();
        Set<UserV2> relatedObject = Collections.singleton(new UserV2());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV2.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();

        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV2.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalWriter.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV3() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV3.class.getName());
        UserV3 instance = new UserV3();
        Set<UserV3> relatedObject = Collections.singleton(new UserV3());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV3.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV3.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV3.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV3.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getFriend()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV4() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV4.class.getName());
        UserV4 instance = new UserV4();
        Set<UserV4> relatedObject = Collections.singleton(new UserV4());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV4.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV4.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV4.class, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV4.class, KNOWS, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getFriend()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV5() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV5.class.getName());
        UserV5 instance = new UserV5();
        Set<UserV5> relatedObject = Collections.singleton(new UserV5());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV5.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV5.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV5.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV5.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getFriend()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV6() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV6.class.getName());
        UserV6 instance = new UserV6();
        Set<UserV6> relatedObject = Collections.singleton(new UserV6());

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV6.class, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV6.class, KNOWS, Relationship.Direction.INCOMING);

        relationalReader = EntityAccessManager.getIterableField(classInfo, UserV6.class, KNOWS, Relationship.Direction.OUTGOING);
        relationalWriter = EntityAccessManager.getIterableField(classInfo, UserV6.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnowsPeople()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV7() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV7.class.getName());
        UserV7 instance = new UserV7();
        Set<UserV7> relatedObject = Collections.singleton(new UserV7());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV7.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV7.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV7.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV7.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV8() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV8.class.getName());
        UserV8 instance = new UserV8();
        Set<UserV8> relatedObject = Collections.singleton(new UserV8());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV8.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV8.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV8.class, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV8.class, KNOWS, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV9() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV9.class.getName());
        UserV9 instance = new UserV9();
        Set<UserV9> relatedObjectOut = Collections.singleton(new UserV9());
        Set<UserV9> relatedObjectIn = Collections.singleton(new UserV9());

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV9.class, LIKES, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV9.class, LIKES, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getLikes()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        relationalReader = EntityAccessManager.getIterableField(classInfo, UserV9.class, LIKES, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager.getIterableField(classInfo, UserV9.class, LIKES, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getLikedBy()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV10() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV10.class.getName());
        UserV10 instance = new UserV10();
        Set<UserV10> relatedObjectOut = Collections.singleton(new UserV10());
        Set<UserV10> relatedObjectIn = Collections.singleton(new UserV10());

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV10.class, LIKES, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV10.class, LIKES, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getLikes()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        relationalReader = EntityAccessManager.getIterableField(classInfo, UserV10.class, LIKES, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager.getIterableField(classInfo, UserV10.class, LIKES, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getLikedBy()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV11() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV11.class.getName());
        UserV11 instance = new UserV11();
        Set<UserV11> relatedObjectOut = Collections.singleton(new UserV11());
        Set<UserV11> relatedObjectIn = Collections.singleton(new UserV11());

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV11.class, LIKES, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV11.class, LIKES, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getFriend()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        relationalReader = EntityAccessManager.getIterableField(classInfo, UserV11.class, LIKES, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager.getIterableField(classInfo, UserV11.class, LIKES, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getFriendOf()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV12() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV12.class.getName());
        UserV12 instance = new UserV12();
        Set<UserV12> relatedObjectOut = Collections.singleton(new UserV12());
        Set<UserV12> relatedObjectIn = Collections.singleton(new UserV12());

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV12.class, LIKES, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV12.class, LIKES, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getFriend()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        relationalReader = EntityAccessManager.getIterableField(classInfo, UserV12.class, LIKES, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager.getIterableField(classInfo, UserV12.class, LIKES, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getFriendOf()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV13() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV13.class.getName());
        UserV13 instance = new UserV13();
        Set<UserV13> relatedObject = Collections.singleton(new UserV13());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV13.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV13.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV13.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV13.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV14() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV14.class.getName());
        UserV14 instance = new UserV14();
        Set<UserV14> relatedObject = Collections.singleton(new UserV14());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV14.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV14.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV14.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV14.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.knows).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV15() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV15.class.getName());
        UserV15 instance = new UserV15();
        Set<UserV15> relatedObjectOut = Collections.singleton(new UserV15());
        Set<UserV15> relatedObjectIn = Collections.singleton(new UserV15());

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV15.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV15.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getKnows()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        instance = new UserV15();

        relationalReader = EntityAccessManager.getIterableField(classInfo, UserV15.class, KNOWS, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager.getIterableField(classInfo, UserV15.class, KNOWS, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getKnows()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV16() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV16.class.getName());
        UserV16 instance = new UserV16();
        Set<UserV16> relatedObjectOut = Collections.singleton(new UserV16());
        Set<UserV16> relatedObjectIn = Collections.singleton(new UserV16());

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV16.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV16.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getKnows()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        instance = new UserV16();

        relationalReader = EntityAccessManager.getIterableField(classInfo, UserV16.class, KNOWS, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager.getIterableField(classInfo, UserV16.class, KNOWS, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getKnows()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV17() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV17.class.getName());
        UserV17 instance = new UserV17();
        Set<UserV17> relatedObject = Collections.singleton(new UserV17());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV17.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV17.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV17.class, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV17.class, KNOWS, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.knows).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV18() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV18.class.getName());
        UserV18 instance = new UserV18();
        Set<UserV18> relatedObject = Collections.singleton(new UserV18());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV18.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV18.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV18.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV18.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV19() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV19.class.getName());
        UserV19 instance = new UserV19();
        Set<UserV19> relatedObject = Collections.singleton(new UserV19());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV19.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV19.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV19.class, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV19.class, KNOWS, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    /**
     * @see issue #36
     */
    @Test
    public void testUserV20() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV20.class.getName());
        UserV20 instance = new UserV20();
        List<PlainUser> relatedObject = Collections.singletonList(new PlainUser());
        UserV20 otherRelatedObject = new UserV20();

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV20.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV20.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, PlainUser.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, PlainUser.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.plainUsers).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);

        instance = new UserV20();
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, otherRelatedObject);

        relationalWriter.write(instance, otherRelatedObject);
        assertThat(instance.user).isEqualTo(otherRelatedObject);
    }

    /**
     * @see issue #36
     */
    @Test
    public void testUserV21() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV21.class.getName());
        UserV21 instance = new UserV21();
        PlainUser relatedObject = new PlainUser();
        List<UserV21> otherRelatedObject = Collections.singletonList(new UserV21());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV21.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV21.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV21.class, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV21.class, KNOWS, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, otherRelatedObject);
        assertThat(instance.user).isEqualTo(otherRelatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(otherRelatedObject);

        instance = new UserV21();
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.plainUsers).isEqualTo(relatedObject);
    }

    /**
     * @see issue #36
     */
    @Test
    public void testUserV22() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV22.class.getName());
        UserV22 instance = new UserV22();
        List<PlainUser> relatedObject = Collections.singletonList(new PlainUser());
        UserV22 otherRelatedObject = new UserV22();

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV22.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV22.class, KNOWS, Relationship.Direction.INCOMING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, PlainUser.class, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, PlainUser.class, KNOWS, Relationship.Direction.OUTGOING);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.plainUsers).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);

        instance = new UserV22();
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, otherRelatedObject);

        relationalWriter.write(instance, otherRelatedObject);
        assertThat(instance.user).isEqualTo(otherRelatedObject);
    }

    /**
     * @see issue #36
     */
    @Test
    public void testUserV23() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV23.class.getName());
        UserV23 instance = new UserV23();
        PlainUser relatedObject = new PlainUser();
        List<UserV23> otherRelatedObject = Collections.singletonList(new UserV23());

        assertThat(EntityAccessManager.getIterableField(classInfo, UserV23.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();
        assertThat(EntityAccessManager.getIterableField(classInfo, UserV23.class, KNOWS, Relationship.Direction.OUTGOING))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getIterableField(classInfo, UserV23.class, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getIterableField(classInfo, UserV23.class, KNOWS, Relationship.Direction.INCOMING);

        relationalWriter.write(instance, otherRelatedObject);
        assertThat(instance.user).isEqualTo(otherRelatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(otherRelatedObject);

        instance = new UserV23();
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.plainUsers).isEqualTo(relatedObject);
    }
}
