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
package org.neo4j.ogm.persistence.examples.canonical;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.canonical.Mappable;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 */
public class CanonicalTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.canonical");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    /**
     * @see Issue #127, Issue #157
     */
    @Test
    public void shouldMapArraysWhenUsingCustomQueries() {

        Mappable mappable = new Mappable();
        mappable.setPrimitiveBoolean(false);
        mappable.setPrimitiveByte((byte) 100);
        mappable.setPrimitiveChar('c');
        mappable.setPrimitiveDouble(123.4);
        mappable.setPrimitiveFloat(567.8f);
        mappable.setPrimitiveInt(5);
        mappable.setPrimitiveLong(8L);
        mappable.setPrimitiveShort((short) 200);

        mappable.setPrimitiveBooleanArray(new boolean[] { true, false });
        mappable.setPrimitiveByteArray(new byte[] { (byte) 10, (byte) 100 });
        mappable.setPrimitiveCharArray(new char[] { 'd', '\u0001' });
        mappable.setPrimitiveDoubleArray(new double[] { 34.5, 67.8 });
        mappable.setPrimitiveFloatArray(new float[] { 1.2f, 3.4f });
        mappable.setPrimitiveIntArray(new int[] { 6, 7 });
        mappable.setPrimitiveLongArray(new long[] { 9, 10 });
        mappable.setPrimitiveShortArray(new short[] { (short) 30, (short) 300 });

        mappable.setObjectBoolean(Boolean.FALSE);
        mappable.setObjectByte(Byte.valueOf("100"));
        mappable.setObjectDouble(567.8);
        mappable.setObjectFloat(123.4f);
        mappable.setObjectInteger(99);
        mappable.setObjectLong(1_000L);
        mappable.setObjectShort(Short.valueOf("100"));
        mappable.setObjectString("abc");
        mappable.setObjectCharacter('d');

        mappable.setObjectBooleanArray(new Boolean[] { Boolean.TRUE, Boolean.FALSE });
        mappable.setObjectByteArray(new Byte[] { (byte) 10, (byte) 100 });
        mappable.setObjectCharArray(new Character[] { 'd', '\u0028' });
        mappable.setObjectDoubleArray(new Double[] { 34.5, 67.8 });
        mappable.setObjectFloatArray(new Float[] { 1.2f, 3.4f });
        mappable.setObjectIntegerArray(new Integer[] { 6, 7 });
        mappable.setObjectLongArray(new Long[] { 9L, 10L });
        mappable.setObjectShortArray(new Short[] { (short) 30, (short) 300 });
        mappable.setObjectStringArray(new String[] { "abc", "xyz" });

        mappable.setListOfString(Arrays.asList("a", "bb", "cc"));
        mappable.setListOfCharacter(Arrays.asList('a', 'b', 'c'));

        session.save(mappable);

        session.clear();

        Result result = session.query("match (n) return n", Collections.EMPTY_MAP);
        assertThat(result).isNotNull();
        Mappable loaded = (Mappable) result.iterator().next().get("n");
        assertThat(loaded).isNotNull();

        assertThat(loaded.isPrimitiveBoolean()).isFalse();
        assertThat(loaded.getPrimitiveByte()).isEqualTo(mappable.getPrimitiveByte());
        assertThat(loaded.getPrimitiveChar()).isEqualTo(mappable.getPrimitiveChar());
        assertThat(loaded.getPrimitiveDouble()).isEqualTo(mappable.getPrimitiveDouble(), within(0.0));
        assertThat(loaded.getPrimitiveFloat()).isEqualTo(mappable.getPrimitiveFloat(), within(0.0f));
        assertThat(loaded.getPrimitiveInt()).isEqualTo(mappable.getPrimitiveInt());
        assertThat(loaded.getPrimitiveLong()).isEqualTo(mappable.getPrimitiveLong());
        assertThat(loaded.getPrimitiveShort()).isEqualTo(mappable.getPrimitiveShort());

        assertThat(loaded.getPrimitiveBooleanArray()).isEqualTo(mappable.getPrimitiveBooleanArray());
        assertThat(loaded.getPrimitiveByteArray()).isEqualTo(mappable.getPrimitiveByteArray());
        assertThat(loaded.getPrimitiveCharArray()).isEqualTo(mappable.getPrimitiveCharArray());
        assertThat(loaded.getPrimitiveDoubleArray()).isEqualTo(mappable.getPrimitiveDoubleArray());
        assertThat(loaded.getPrimitiveFloatArray()).isEqualTo(mappable.getPrimitiveFloatArray());
        assertThat(loaded.getPrimitiveIntArray()).isEqualTo(mappable.getPrimitiveIntArray());
        assertThat(loaded.getPrimitiveLongArray()).isEqualTo(mappable.getPrimitiveLongArray());
        assertThat(loaded.getPrimitiveShortArray()).isEqualTo(mappable.getPrimitiveShortArray());

        assertThat(loaded.getObjectBoolean()).isEqualTo(mappable.getObjectBoolean());
        assertThat(loaded.getObjectByte()).isEqualTo(mappable.getObjectByte());
        assertThat(loaded.getObjectDouble()).isEqualTo(mappable.getObjectDouble());
        assertThat(loaded.getObjectFloat()).isEqualTo(mappable.getObjectFloat());
        assertThat(loaded.getObjectInteger()).isEqualTo(mappable.getObjectInteger());
        assertThat(loaded.getObjectLong()).isEqualTo(mappable.getObjectLong());
        assertThat(loaded.getObjectShort()).isEqualTo(mappable.getObjectShort());
        assertThat(loaded.getObjectString()).isEqualTo(mappable.getObjectString());
        assertThat(loaded.getObjectCharacter()).isEqualTo(mappable.getObjectCharacter());

        assertThat(loaded.getObjectBooleanArray()).isEqualTo(mappable.getObjectBooleanArray());
        assertThat(loaded.getObjectByteArray()).isEqualTo(mappable.getObjectByteArray());
        assertThat(loaded.getObjectCharArray()).isEqualTo(mappable.getObjectCharArray());
        assertThat(loaded.getObjectDoubleArray()).isEqualTo(mappable.getObjectDoubleArray());
        assertThat(loaded.getObjectFloatArray()).isEqualTo(mappable.getObjectFloatArray());
        assertThat(loaded.getObjectIntegerArray()).isEqualTo(mappable.getObjectIntegerArray());
        assertThat(loaded.getObjectLongArray()).isEqualTo(mappable.getObjectLongArray());
        assertThat(loaded.getObjectShortArray()).isEqualTo(mappable.getObjectShortArray());
        assertThat(loaded.getObjectStringArray()).isEqualTo(mappable.getObjectStringArray());

        assertThat(loaded.getListOfString()).isEqualTo(mappable.getListOfString());
        assertThat(loaded.getListOfCharacter()).isEqualTo(mappable.getListOfCharacter());
    }
}
