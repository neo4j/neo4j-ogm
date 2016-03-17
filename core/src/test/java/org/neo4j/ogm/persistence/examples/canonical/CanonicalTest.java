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

package org.neo4j.ogm.persistence.examples.canonical;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.canonical.Mappable;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class CanonicalTest extends MultiDriverTestClass {
	private Session session;

	@Before
	public void init() throws IOException {
		session = new SessionFactory("org.neo4j.ogm.domain.canonical").openSession();
	}

	/**
	 * @see Issue #127
	 */
	@Test
	public void shouldMapArraysWhenUsingCustomQueries() {

		Mappable mappable = new Mappable();
		mappable.setPrimitiveBoolean(false);
		mappable.setPrimitiveByte((byte)100);
		mappable.setPrimitiveChar('c');
		mappable.setPrimitiveDouble(123.4);
		mappable.setPrimitiveFloat(567.8f);
		mappable.setPrimitiveInt(5);
		mappable.setPrimitiveLong(8l);
		mappable.setPrimitiveShort((short)200);

		mappable.setPrimitiveBooleanArray(new boolean[] {true, false});
		mappable.setPrimitiveByteArray(new byte[] {(byte)10, (byte)100});
		mappable.setPrimitiveCharArray(new char[] {'d','\u0001'});
		mappable.setPrimitiveDoubleArray(new double[] {34.5, 67.8});
		mappable.setPrimitiveFloatArray(new float[] {1.2f,3.4f});
		mappable.setPrimitiveIntArray(new int[] {6,7});
		mappable.setPrimitiveLongArray(new long[] {9,10});
		mappable.setPrimitiveShortArray(new short[] {(short)30, (short)300});

		mappable.setObjectBoolean(Boolean.FALSE);
		mappable.setObjectByte(Byte.valueOf("100"));
		mappable.setObjectDouble(567.8);
		mappable.setObjectFloat(123.4f);
		mappable.setObjectInteger(99);
		mappable.setObjectLong(1000l);
		mappable.setObjectShort(Short.valueOf("100"));
		mappable.setObjectString("abc");
		mappable.setObjectCharacter('d');

		mappable.setObjectBooleanArray(new Boolean[] {Boolean.TRUE, Boolean.FALSE});
		mappable.setObjectByteArray(new Byte[] {(byte)10, (byte)100});
		mappable.setObjectCharArray(new Character[] {'d','\u0028'});
		mappable.setObjectDoubleArray(new Double[] {34.5, 67.8});
		mappable.setObjectFloatArray(new Float[] {1.2f,3.4f});
		mappable.setObjectIntegerArray(new Integer[] {6,7});
		mappable.setObjectLongArray(new Long[] {9l,10l});
		mappable.setObjectShortArray(new Short[] {(short)30, (short)300});
		mappable.setObjectStringArray(new String[] {"abc", "xyz"});
		session.save(mappable);

		session.clear();

		Result result = session.query("match (n) return n", Collections.EMPTY_MAP);
		assertNotNull(result);
		Mappable loaded = (Mappable) result.iterator().next().get("n");
		assertNotNull(loaded);

		assertFalse(loaded.isPrimitiveBoolean());
		assertEquals(mappable.getPrimitiveByte(), loaded.getPrimitiveByte());
		assertEquals(mappable.getPrimitiveChar(), loaded.getPrimitiveChar());
		assertEquals(mappable.getPrimitiveDouble(), loaded.getPrimitiveDouble(), 0);
		assertEquals(mappable.getPrimitiveFloat(), loaded.getPrimitiveFloat(), 0);
		assertEquals(mappable.getPrimitiveInt(), loaded.getPrimitiveInt());
		assertEquals(mappable.getPrimitiveLong(), loaded.getPrimitiveLong());
		assertEquals(mappable.getPrimitiveShort(), loaded.getPrimitiveShort());

		assertArrayEquals(mappable.getPrimitiveBooleanArray(), loaded.getPrimitiveBooleanArray());
		assertArrayEquals(mappable.getPrimitiveByteArray(), loaded.getPrimitiveByteArray());
		assertArrayEquals(mappable.getPrimitiveCharArray(), loaded.getPrimitiveCharArray());
		assertArrayEquals(mappable.getPrimitiveDoubleArray(), loaded.getPrimitiveDoubleArray(), 0);
		assertArrayEquals(mappable.getPrimitiveFloatArray(), loaded.getPrimitiveFloatArray(), 0);
		assertArrayEquals(mappable.getPrimitiveIntArray(), loaded.getPrimitiveIntArray());
		assertArrayEquals(mappable.getPrimitiveLongArray(), loaded.getPrimitiveLongArray());
		assertArrayEquals(mappable.getPrimitiveShortArray(), loaded.getPrimitiveShortArray());

		assertEquals(mappable.getObjectBoolean(), loaded.getObjectBoolean());
		assertEquals(mappable.getObjectByte(), loaded.getObjectByte());
		assertEquals(mappable.getObjectDouble(), loaded.getObjectDouble());
		assertEquals(mappable.getObjectFloat(), loaded.getObjectFloat());
		assertEquals(mappable.getObjectInteger(), loaded.getObjectInteger());
		assertEquals(mappable.getObjectLong(), loaded.getObjectLong());
		assertEquals(mappable.getObjectShort(), loaded.getObjectShort());
		assertEquals(mappable.getObjectString(), loaded.getObjectString());
		assertEquals(mappable.getObjectCharacter(), loaded.getObjectCharacter());


		assertArrayEquals(mappable.getObjectBooleanArray(), loaded.getObjectBooleanArray());
		assertArrayEquals(mappable.getObjectByteArray(), loaded.getObjectByteArray());
		assertArrayEquals(mappable.getObjectCharArray(), loaded.getObjectCharArray());
		assertArrayEquals(mappable.getObjectDoubleArray(), loaded.getObjectDoubleArray());
		assertArrayEquals(mappable.getObjectFloatArray(), loaded.getObjectFloatArray());
		assertArrayEquals(mappable.getObjectIntegerArray(), loaded.getObjectIntegerArray());
		assertArrayEquals(mappable.getObjectLongArray(), loaded.getObjectLongArray());
		assertArrayEquals(mappable.getObjectShortArray(), loaded.getObjectShortArray());
		assertArrayEquals(mappable.getObjectStringArray(), loaded.getObjectStringArray());
	}

}
