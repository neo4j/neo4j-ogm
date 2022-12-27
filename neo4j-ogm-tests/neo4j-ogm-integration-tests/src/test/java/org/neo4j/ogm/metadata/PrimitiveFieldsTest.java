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
public class PrimitiveFieldsTest extends TestMetaDataTypeResolution {

    @Test
    void testPrimitiveShortArray() {
        checkField("pss", "short[]", short.class);
    }

    @Test
    void testPrimitiveCharArray() {
        checkField("pcc", "char[]", char.class);
    }

    @Test
    void testPrimitiveByteArray() {
        checkField("pbb", "byte[]", byte.class);
    }

    @Test
    void testPrimitiveLongArray() {
        checkField("pll", "long[]", long.class);
    }

    @Test
    void testPrimitiveDoubleArray() {
        checkField("pdd", "double[]", double.class);
    }

    @Test
    void testPrimitiveFloatArray() {
        checkField("pff", "float[]", float.class);
    }

    @Test
    void testPrimitiveBooleanArray() {
        checkField("pzz", "boolean[]", boolean.class);
    }

    @Test
    void testPrimitiveIntegerArray() {
        checkField("pii", "int[]", int.class);
    }

    @Test
    void testPrimitiveShort() {
        checkField("ps", "short", short.class);
    }

    @Test
    void testPrimitiveChar() {
        checkField("pc", "char", char.class);
    }

    @Test
    void testPrimitiveByte() {
        checkField("pb", "byte", byte.class);
    }

    @Test
    void testPrimitiveLong() {
        checkField("pl", "long", long.class);
    }

    @Test
    void testPrimitiveDouble() {
        checkField("pd", "double", double.class);
    }

    @Test
    void testPrimitiveFloat() {
        checkField("pf", "float", float.class);
    }

    @Test
    void testPrimitiveBoolean() {
        checkField("pz", "boolean", boolean.class);
    }

    @Test
    void testPrimitiveInteger() {
        checkField("pi", "int", int.class);
    }
}
