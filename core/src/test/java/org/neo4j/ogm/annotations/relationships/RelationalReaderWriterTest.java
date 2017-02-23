/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.annotations.relationships;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.entityMapping.*;
import org.neo4j.ogm.entity.io.EntityAccessManager;
import org.neo4j.ogm.entity.io.FieldReader;
import org.neo4j.ogm.entity.io.FieldWriter;
import org.neo4j.ogm.entity.io.FieldReader;
import org.neo4j.ogm.entity.io.FieldWriter;
import org.neo4j.ogm.entity.io.RelationalReader;
import org.neo4j.ogm.entity.io.RelationalWriter;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;

/**
 * @author Luanne Misquitta
 */
public class RelationalReaderWriterTest {

    final String KNOWS = "KNOWS";
    final String LIKES = "LIKES";
    private EntityAccessManager entityAccessStrategy;
    private DomainInfo domainInfo;

    @Before
    public void setup() {
        entityAccessStrategy = new EntityAccessManager();
        domainInfo = new DomainInfo("org.neo4j.ogm.domain.entityMapping");
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV1() {
        final String KNOWN_BY = "KNOWN_BY";

        ClassInfo classInfo = this.domainInfo.getClass(UserV1.class.getName());
        UserV1 instance = new UserV1();
        UserV1 relatedObject = new UserV1();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWN_BY, Relationship.OUTGOING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWN_BY, Relationship.OUTGOING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWN_BY, Relationship.INCOMING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWN_BY, Relationship.INCOMING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getKnownBy());
        assertEquals(relatedObject, relationalReader.read(instance));

    }


    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV2() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV2.class.getName());
        UserV2 instance = new UserV2();
        UserV2 relatedObject = new UserV2();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getKnows());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV3() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV3.class.getName());
        UserV3 instance = new UserV3();
        UserV3 relatedObject = new UserV3();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getFriend());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV4() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV4.class.getName());
        UserV4 instance = new UserV4();
        UserV4 relatedObject = new UserV4();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getFriend());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV5() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV5.class.getName());
        UserV5 instance = new UserV5();
        UserV5 relatedObject = new UserV5();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getFriend());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV6() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV6.class.getName());
        UserV6 instance = new UserV6();
        UserV6 relatedObject = new UserV6();

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getKnowsPerson());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV7() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV7.class.getName());
        UserV7 instance = new UserV7();
        UserV7 relatedObject = new UserV7();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getKnows());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV8() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV8.class.getName());
        UserV8 instance = new UserV8();
        UserV8 relatedObject = new UserV8();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getKnows());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV9() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV9.class.getName());
        UserV9 instance = new UserV9();
        UserV9 relatedObjectOut = new UserV9();
        UserV9 relatedObjectIn = new UserV9();

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, LIKES, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, LIKES, Relationship.OUTGOING, relatedObjectOut);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectOut);
        assertEquals(relatedObjectOut, instance.getLikes());
        assertEquals(relatedObjectOut, relationalReader.read(instance));

        relationalReader = entityAccessStrategy.getRelationalReader(classInfo, LIKES, Relationship.INCOMING);
        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, LIKES, Relationship.INCOMING, relatedObjectIn);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectIn);
        assertEquals(relatedObjectIn, instance.getLikedBy());
        assertEquals(relatedObjectIn, relationalReader.read(instance));
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV10() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV10.class.getName());
        UserV10 instance = new UserV10();
        UserV10 relatedObjectOut = new UserV10();
        UserV10 relatedObjectIn = new UserV10();

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, LIKES, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, LIKES, Relationship.OUTGOING, relatedObjectOut);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectOut);
        assertEquals(relatedObjectOut, instance.getLikes());
        assertEquals(relatedObjectOut, relationalReader.read(instance));

        relationalReader = entityAccessStrategy.getRelationalReader(classInfo, LIKES, Relationship.INCOMING);
        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, LIKES, Relationship.INCOMING, relatedObjectIn);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectIn);
        assertEquals(relatedObjectIn, instance.getLikedBy());
        assertEquals(relatedObjectIn, relationalReader.read(instance));
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV11() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV11.class.getName());
        UserV11 instance = new UserV11();
        UserV11 relatedObjectOut = new UserV11();
        UserV11 relatedObjectIn = new UserV11();

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, LIKES, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, LIKES, Relationship.OUTGOING, relatedObjectOut);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectOut);
        assertEquals(relatedObjectOut, instance.getFriend());
        assertEquals(relatedObjectOut, relationalReader.read(instance));

        relationalReader = entityAccessStrategy.getRelationalReader(classInfo, LIKES, Relationship.INCOMING);
        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, LIKES, Relationship.INCOMING, relatedObjectIn);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectIn);
        assertEquals(relatedObjectIn, instance.getFriendOf());
        assertEquals(relatedObjectIn, relationalReader.read(instance));
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV12() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV12.class.getName());
        UserV12 instance = new UserV12();
        UserV12 relatedObjectOut = new UserV12();
        UserV12 relatedObjectIn = new UserV12();

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, LIKES, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, LIKES, Relationship.OUTGOING, relatedObjectOut);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectOut);
        assertEquals(relatedObjectOut, instance.getFriend());
        assertEquals(relatedObjectOut, relationalReader.read(instance));

        relationalReader = entityAccessStrategy.getRelationalReader(classInfo, LIKES, Relationship.INCOMING);
        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, LIKES, Relationship.INCOMING, relatedObjectIn);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectIn);
        assertEquals(relatedObjectIn, instance.getFriendOf());
        assertEquals(relatedObjectIn, relationalReader.read(instance));
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV13() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV13.class.getName());
        UserV13 instance = new UserV13();
        UserV13 relatedObject = new UserV13();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getKnows());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV14() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV14.class.getName());
        UserV14 instance = new UserV14();
        UserV14 relatedObject = new UserV14();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.knows);
        assertEquals(relatedObject, relationalReader.read(instance));
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV15() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV15.class.getName());
        UserV15 instance = new UserV15();
        UserV15 relatedObjectOut = new UserV15();
        UserV15 relatedObjectIn = new UserV15();

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObjectOut);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectOut);
        assertEquals(relatedObjectOut, instance.getKnows());
        assertEquals(relatedObjectOut, relationalReader.read(instance));

        instance = new UserV15();

        relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING);
        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObjectIn);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectIn);
        assertEquals(relatedObjectIn, instance.getKnows());
        assertEquals(relatedObjectIn, relationalReader.read(instance));
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV16() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV16.class.getName());
        UserV16 instance = new UserV16();
        UserV16 relatedObjectOut = new UserV16();
        UserV16 relatedObjectIn = new UserV16();

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObjectOut);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectOut);
        assertEquals(relatedObjectOut, instance.getKnows());
        assertEquals(relatedObjectOut, relationalReader.read(instance));

        instance = new UserV16();

        relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING);
        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObjectIn);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObjectIn);
        assertEquals(relatedObjectIn, instance.getKnows());
        assertEquals(relatedObjectIn, relationalReader.read(instance));
    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV17() {
        ClassInfo classInfo = this.domainInfo.getClass(UserV17.class.getName());
        UserV17 instance = new UserV17();
        UserV17 relatedObject = new UserV17();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.knows);
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV18() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV18.class.getName());
        UserV18 instance = new UserV18();
        UserV18 relatedObject = new UserV18();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getKnows());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see DATAGRAPH-636
     */
    @Test
    public void testUserV19() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV19.class.getName());
        UserV19 instance = new UserV19();
        UserV19 relatedObject = new UserV19();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject));

        RelationalReader relationalReader = entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING);
        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject);

        assertTrue(relationalReader instanceof FieldReader);
        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getKnows());
        assertEquals(relatedObject, relationalReader.read(instance));

    }

    /**
     * @see issue #36
     */
    @Test
    public void testUserV20() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV20.class.getName());
        UserV20 instance = new UserV20();
        UserV20 relatedObject = new UserV20();
        PlainUser otherRelatedObject = new PlainUser();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.OUTGOING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject));

        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject);

        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getUser());

        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, otherRelatedObject));

        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, otherRelatedObject);

        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, otherRelatedObject);
        assertEquals(otherRelatedObject, instance.getPlainUser());
    }

    /**
     * @see issue #36
     */
    @Test
    public void testUserV21() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV21.class.getName());
        UserV21 instance = new UserV21();
        UserV21 relatedObject = new UserV21();
        PlainUser otherRelatedObject = new PlainUser();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject));

        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getUser());

        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, otherRelatedObject));

        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, otherRelatedObject);

        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, otherRelatedObject);
        assertEquals(otherRelatedObject, instance.getPlainUser());
    }


    /**
     * @see issue #36
     */
    @Test
    public void testUserV22() {

        ClassInfo classInfo = this.domainInfo.getClass(UserV22.class.getName());
        UserV22 instance = new UserV22();
        UserV22 relatedObject = new UserV22();
        PlainUser otherRelatedObject = new PlainUser();

        assertNull(entityAccessStrategy.getRelationalReader(classInfo, KNOWS, Relationship.INCOMING));
        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, relatedObject));

        RelationalWriter relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, relatedObject);

        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, relatedObject);
        assertEquals(relatedObject, instance.getUser());

        assertNull(entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.INCOMING, otherRelatedObject));

        relationalWriter = entityAccessStrategy.getRelationalWriter(classInfo, KNOWS, Relationship.OUTGOING, otherRelatedObject);

        assertTrue(relationalWriter instanceof FieldWriter);

        relationalWriter.write(instance, otherRelatedObject);
        assertEquals(otherRelatedObject, instance.getPlainUser());
    }

}
