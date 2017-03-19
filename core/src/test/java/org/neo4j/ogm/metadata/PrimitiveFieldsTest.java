/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.metadata;

import org.junit.Test;

/**
 * @author vince
 */
public class PrimitiveFieldsTest extends TestMetaDataTypeResolution {

    @Test
    public void testPrimitiveShortArray() {
        checkField("pss", "short[]", short.class);
    }

    @Test
    public void testPrimitiveCharArray() {
        checkField("pcc", "char[]", char.class);
    }

    @Test
    public void testPrimitiveByteArray() {
        checkField("pbb", "byte[]", byte.class);
    }

    @Test
    public void testPrimitiveLongArray() {
        checkField("pll", "long[]", long.class);
    }

    @Test
    public void testPrimitiveDoubleArray() {
        checkField("pdd", "double[]", double.class);
    }

    @Test
    public void testPrimitiveFloatArray() {
        checkField("pff", "float[]", float.class);
    }

    @Test
    public void testPrimitiveBooleanArray() {
        checkField("pzz", "boolean[]", boolean.class);
    }

    @Test
    public void testPrimitiveIntegerArray() {
        checkField("pii", "int[]", int.class);
    }

    @Test
    public void testPrimitiveShort() {
        checkField("ps", "short", short.class);
    }

    @Test
    public void testPrimitiveChar() {
        checkField("pc", "char", char.class);
    }

    @Test
    public void testPrimitiveByte() {
        checkField("pb", "byte", byte.class);
    }

    @Test
    public void testPrimitiveLong() {
        checkField("pl", "long", long.class);
    }

    @Test
    public void testPrimitiveDouble() {
        checkField("pd", "double", double.class);
    }

    @Test
    public void testPrimitiveFloat() {
        checkField("pf", "float", float.class);
    }

    @Test
    public void testPrimitiveBoolean() {
        checkField("pz", "boolean", boolean.class);
    }

    @Test
    public void testPrimitiveInteger() {
        checkField("pi", "int", int.class);
    }
}
