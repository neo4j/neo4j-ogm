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

package org.neo4j.ogm.metadata.types;

import org.junit.Test;

/**
 * @author vince
 */
public class AutoboxedFieldsTest extends TestMetaDataTypeResolution {

	@Test
	public void testStringArray () {
		checkField("xx", "[Ljava/lang/String;", String.class);
	}

	@Test
	public void testShortArray() {
		checkField("ss", "[Ljava/lang/Short;", Short.class);
	}

	@Test
	public void testCharacterArray() {
		checkField("cc", "[Ljava/lang/Character;", Character.class);
	}

	@Test
	public void testByteArray() {
		checkField("bb", "[Ljava/lang/Byte;", Byte.class);
	}

	@Test
	public void testLongArray() {
		checkField("ll", "[Ljava/lang/Long;", Long.class);
	}

	@Test
	public void testDoubleArray() {
		checkField("dd", "[Ljava/lang/Double;", Double.class);
	}

	@Test
	public void testFloatArray() {
		checkField("ff", "[Ljava/lang/Float;", Float.class);
	}

	@Test
	public void testBooleanArray() {
		checkField("zz", "[Ljava/lang/Boolean;", Boolean.class);
	}

	@Test
	public void testIntegerArray() {
		checkField("ii", "[Ljava/lang/Integer;", Integer.class);
	}

	@Test
	public void testString() {
		checkField("x", "Ljava/lang/String;", String.class);
	}

	@Test
	public void testShort() {
		checkField("s", "Ljava/lang/Short;", Short.class);
	}

	@Test
	public void testCharacter() {
		checkField("c", "Ljava/lang/Character;", Character.class);
	}

	@Test
	public void testByte() {
		checkField("b", "Ljava/lang/Byte;", Byte.class);
	}

	@Test
	public void testLong() {
		checkField("l", "Ljava/lang/Long;", Long.class);
	}

	@Test
	public void testDouble() {
		checkField("d", "Ljava/lang/Double;", Double.class);
	}

	@Test
	public void testFloat() {
		checkField("f", "Ljava/lang/Float;", Float.class);
	}

	@Test
	public void testBoolean() {
		checkField("z", "Ljava/lang/Boolean;", Boolean.class);
	}

	@Test
	public void testInteger() {
		checkField("i", "Ljava/lang/Integer;", Integer.class);
	}

}
