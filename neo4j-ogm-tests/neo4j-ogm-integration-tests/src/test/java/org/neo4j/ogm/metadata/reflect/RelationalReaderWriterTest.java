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

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.entityMapping.*;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.FieldInfo;

/**
 * @author Luanne Misquitta
 */
public class RelationalReaderWriterTest {

    final String KNOWS = "KNOWS";
    final String LIKES = "LIKES";
    private DomainInfo domainInfo;

    @Before
    public void setup() {
        domainInfo = DomainInfo.create("org.neo4j.ogm.domain.entityMapping");
    }

    @Test // DATAGRAPH-636
    public void testUserV1() {
        final String KNOWN_BY = "KNOWN_BY";

        ClassInfo classInfo = this.domainInfo.getClass(UserV1.class.getName());
        UserV1 instance = new UserV1();
        UserV1 relatedObject = new UserV1();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWN_BY, Relationship.Direction.OUTGOING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWN_BY, Relationship.Direction.OUTGOING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager
            .getRelationalReader(classInfo, KNOWN_BY, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWN_BY, Relationship.Direction.INCOMING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnownBy()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV2() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV2.class.getName());
        UserV2 instance = new UserV2();
        UserV2 relatedObject = new UserV2();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV3() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV3.class.getName());
        UserV3 instance = new UserV3();
        UserV3 relatedObject = new UserV3();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getFriend()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV4() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV4.class.getName());
        UserV4 instance = new UserV4();
        UserV4 relatedObject = new UserV4();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getFriend()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV5() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV5.class.getName());
        UserV5 instance = new UserV5();
        UserV5 relatedObject = new UserV5();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getFriend()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV6() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV6.class.getName());
        UserV6 instance = new UserV6();
        UserV6 relatedObject = new UserV6();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnowsPerson()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV7() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV7.class.getName());
        UserV7 instance = new UserV7();
        UserV7 relatedObject = new UserV7();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV8() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV8.class.getName());
        UserV8 instance = new UserV8();
        UserV8 relatedObject = new UserV8();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV9() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV9.class.getName());
        UserV9 instance = new UserV9();
        UserV9 relatedObjectOut = new UserV9();
        UserV9 relatedObjectIn = new UserV9();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, LIKES, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, LIKES, Relationship.Direction.OUTGOING, relatedObjectOut);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getLikes()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        relationalReader = EntityAccessManager.getRelationalReader(classInfo, LIKES, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, LIKES, Relationship.Direction.INCOMING, relatedObjectIn);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getLikedBy()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    @Test // DATAGRAPH-636
    public void testUserV10() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV10.class.getName());
        UserV10 instance = new UserV10();
        UserV10 relatedObjectOut = new UserV10();
        UserV10 relatedObjectIn = new UserV10();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, LIKES, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, LIKES, Relationship.Direction.OUTGOING, relatedObjectOut);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getLikes()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        relationalReader = EntityAccessManager.getRelationalReader(classInfo, LIKES, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, LIKES, Relationship.Direction.INCOMING, relatedObjectIn);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getLikedBy()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    @Test // DATAGRAPH-636
    public void testUserV11() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV11.class.getName());
        UserV11 instance = new UserV11();
        UserV11 relatedObjectOut = new UserV11();
        UserV11 relatedObjectIn = new UserV11();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, LIKES, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, LIKES, Relationship.Direction.OUTGOING, relatedObjectOut);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getFriend()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        relationalReader = EntityAccessManager.getRelationalReader(classInfo, LIKES, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, LIKES, Relationship.Direction.INCOMING, relatedObjectIn);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getFriendOf()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    @Test // DATAGRAPH-636
    public void testUserV12() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV12.class.getName());
        UserV12 instance = new UserV12();
        UserV12 relatedObjectOut = new UserV12();
        UserV12 relatedObjectIn = new UserV12();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, LIKES, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, LIKES, Relationship.Direction.OUTGOING, relatedObjectOut);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getFriend()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        relationalReader = EntityAccessManager.getRelationalReader(classInfo, LIKES, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, LIKES, Relationship.Direction.INCOMING, relatedObjectIn);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getFriendOf()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    @Test // DATAGRAPH-636
    public void testUserV13() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV13.class.getName());
        UserV13 instance = new UserV13();
        UserV13 relatedObject = new UserV13();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV14() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV14.class.getName());
        UserV14 instance = new UserV14();
        UserV14 relatedObject = new UserV14();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.knows).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV15() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV15.class.getName());
        UserV15 instance = new UserV15();
        UserV15 relatedObjectOut = new UserV15();
        UserV15 relatedObjectIn = new UserV15();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObjectOut);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getKnows()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        instance = new UserV15();

        relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObjectIn);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getKnows()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    @Test // DATAGRAPH-636
    public void testUserV16() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV16.class.getName());
        UserV16 instance = new UserV16();
        UserV16 relatedObjectOut = new UserV16();
        UserV16 relatedObjectIn = new UserV16();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObjectOut);

        relationalWriter.write(instance, relatedObjectOut);
        assertThat(instance.getKnows()).isEqualTo(relatedObjectOut);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectOut);

        instance = new UserV16();

        relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING);
        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObjectIn);

        relationalWriter.write(instance, relatedObjectIn);
        assertThat(instance.getKnows()).isEqualTo(relatedObjectIn);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObjectIn);
    }

    @Test // DATAGRAPH-636
    public void testUserV17() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV17.class.getName());
        UserV17 instance = new UserV17();
        UserV17 relatedObject = new UserV17();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.knows).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV18() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV18.class.getName());
        UserV18 instance = new UserV18();
        UserV18 relatedObject = new UserV18();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // DATAGRAPH-636
    public void testUserV19() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV19.class.getName());
        UserV19 instance = new UserV19();
        UserV19 relatedObject = new UserV19();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject))
            .isNull();

        FieldInfo relationalReader = EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING);
        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getKnows()).isEqualTo(relatedObject);
        assertThat(relationalReader.read(instance)).isEqualTo(relatedObject);
    }

    @Test // GH-36
    public void testUserV20() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV20.class.getName());
        UserV20 instance = new UserV20();
        UserV20 relatedObject = new UserV20();
        PlainUser otherRelatedObject = new PlainUser();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.OUTGOING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject))
            .isNull();

        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getUser()).isEqualTo(relatedObject);

        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, otherRelatedObject))
            .isNull();

        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, otherRelatedObject);

        relationalWriter.write(instance, otherRelatedObject);
        assertThat(instance.getPlainUser()).isEqualTo(otherRelatedObject);
    }

    @Test // GH-36
    public void testUserV21() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV21.class.getName());
        UserV21 instance = new UserV21();
        UserV21 relatedObject = new UserV21();
        PlainUser otherRelatedObject = new PlainUser();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject))
            .isNull();

        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getUser()).isEqualTo(relatedObject);

        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, otherRelatedObject))
            .isNull();

        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, otherRelatedObject);

        relationalWriter.write(instance, otherRelatedObject);
        assertThat(instance.getPlainUser()).isEqualTo(otherRelatedObject);
    }

    @Test // GH-36
    public void testUserV22() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV22.class.getName());
        UserV22 instance = new UserV22();
        UserV22 relatedObject = new UserV22();
        PlainUser otherRelatedObject = new PlainUser();

        assertThat(EntityAccessManager.getRelationalReader(classInfo, KNOWS, Relationship.Direction.INCOMING)).isNull();
        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, relatedObject))
            .isNull();

        FieldInfo relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, relatedObject);

        relationalWriter.write(instance, relatedObject);
        assertThat(instance.getUser()).isEqualTo(relatedObject);

        assertThat(EntityAccessManager.getRelationalWriter(classInfo, KNOWS, Relationship.Direction.INCOMING, otherRelatedObject))
            .isNull();

        relationalWriter = EntityAccessManager
            .getRelationalWriter(classInfo, KNOWS, Relationship.Direction.OUTGOING, otherRelatedObject);

        relationalWriter.write(instance, otherRelatedObject);
        assertThat(instance.getPlainUser()).isEqualTo(otherRelatedObject);
    }
}
