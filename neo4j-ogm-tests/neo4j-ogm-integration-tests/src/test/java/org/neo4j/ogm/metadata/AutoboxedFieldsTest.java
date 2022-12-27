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
package org.neo4j.ogm.metadata;

import org.junit.jupiter.api.Test;

/**
 * @author vince
 */
public class AutoboxedFieldsTest extends TestMetaDataTypeResolution {

    @Test
    void testObjectArray() {
        checkField("oo", "java.lang.Object[]", Object.class);
    }

    @Test
    void testStringArray() {
        checkField("xx", "java.lang.String[]", String.class);
    }

    @Test
    void testShortArray() {
        checkField("ss", "java.lang.Short[]", Short.class);
    }

    @Test
    void testCharacterArray() {
        checkField("cc", "java.lang.Character[]", Character.class);
    }

    @Test
    void testByteArray() {
        checkField("bb", "java.lang.Byte[]", Byte.class);
    }

    @Test
    void testLongArray() {
        checkField("ll", "java.lang.Long[]", Long.class);
    }

    @Test
    void testDoubleArray() {
        checkField("dd", "java.lang.Double[]", Double.class);
    }

    @Test
    void testFloatArray() {
        checkField("ff", "java.lang.Float[]", Float.class);
    }

    @Test
    void testBooleanArray() {
        checkField("zz", "java.lang.Boolean[]", Boolean.class);
    }

    @Test
    void testIntegerArray() {
        checkField("ii", "java.lang.Integer[]", Integer.class);
    }

    @Test
    void testObject() {
        checkField("o", "java.lang.Object", Object.class);
    }

    @Test
    void testString() {
        checkField("x", "java.lang.String", String.class);
    }

    @Test
    void testShort() {
        checkField("s", "java.lang.Short", Short.class);
    }

    @Test
    void testCharacter() {
        checkField("c", "java.lang.Character", Character.class);
    }

    @Test
    void testByte() {
        checkField("b", "java.lang.Byte", Byte.class);
    }

    @Test
    void testLong() {
        checkField("l", "java.lang.Long", Long.class);
    }

    @Test
    void testDouble() {
        checkField("d", "java.lang.Double", Double.class);
    }

    @Test
    void testFloat() {
        checkField("f", "java.lang.Float", Float.class);
    }

    @Test
    void testBoolean() {
        checkField("z", "java.lang.Boolean", Boolean.class);
    }

    @Test
    void testInteger() {
        checkField("i", "java.lang.Integer", Integer.class);
    }
}
