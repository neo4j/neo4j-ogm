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

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

/**
 * @author vince
 */
public class GenericsMethodParameterTypesTest extends TestMetaDataTypeResolution {

	@Test    // <S> Iterable<S> map(Class<S> key, Node<S, T, U> value)
	public void testIterableOfMapOfParameterizedClassesMethod() {
		checkMethod("setMap", "+Ljava/lang/Long;", Object.class);
	}

	@Test
	public void testPrimitiveShortArray() {
		checkMethod("setPss", "([S)V", short[].class);
	}

	@Test
	public void testPrimitiveCharArray() {
		checkMethod("setPcc", "([C)V", char[].class);
	}

	@Test
	public void testPrimitiveByteArray() {
		checkMethod("setPbb", "([B)V", byte[].class);
	}

	@Test
	public void testPrimitiveLongArray() {
		checkMethod("setPll", "([J)V", long[].class);
	}

	@Test
	public void testPrimitiveDoubleArray() {
		checkMethod("setPdd", "([D)V", double[].class);
	}

	@Test
	public void testPrimitiveFloatArray() {
		checkMethod("setPff", "([F)V", float[].class);
	}

	@Test
	public void testPrimitiveBooleanArray() {
		checkMethod("setPzz", "([Z)V", boolean[].class);
	}

	@Test
	public void testPrimitiveIntegerArray() {
		checkMethod("setPii", "([I)V", int[].class);
	}

	@Test
	public void testPrimitiveShort() {
		checkMethod("setPs", "(S)V", short.class);
	}

	@Test
	public void testPrimitiveChar() {
		checkMethod("setPc", "(C)V", char.class);
	}

	@Test
	public void testPrimitiveByte() {
		checkMethod("setPb", "(B)V", byte.class);
	}

	@Test
	public void testPrimitiveLong() {
		checkMethod("setPl", "(J)V", long.class);
	}

	@Test
	public void testPrimitiveDouble() {
		checkMethod("setPd", "(D)V", double.class);
	}

	@Test
	public void testPrimitiveFloat() {
		checkMethod("setPf", "(F)V", float.class);
	}

	@Test
	public void testPrimitiveBoolean() {
		checkMethod("setPz", "(Z)V", boolean.class);
	}

	@Test
	public void testPrimitiveInteger() {
		checkMethod("setPi", "(I)V", int.class);
	}

	@Test
	public void testObjectArray () {
		checkMethod("setOo", "([Ljava/lang/Object;)V", Object[].class);
	}

	@Test
	public void testStringArray () {
		checkMethod("setXx", "([Ljava/lang/String;)V", String[].class);
	}

	@Test
	public void testShortArray() {
		checkMethod("setSs", "([Ljava/lang/Short;)V", Short[].class);
	}

	@Test
	public void testCharacterArray() {
		checkMethod("setCc", "([Ljava/lang/Character;)V", Character[].class);
	}                

	@Test
	public void testByteArray() {
		checkMethod("setBb", "([Ljava/lang/Byte;)V", Byte[].class);
	}

	@Test
	public void testLongArray() {
		checkMethod("setLl", "([Ljava/lang/Long;)V", Long[].class);
	}

	@Test
	public void testDoubleArray() {
		checkMethod("setDd", "([Ljava/lang/Double;)V", Double[].class);
	}

	@Test
	public void testFloatArray() {
		checkMethod("setFf", "([Ljava/lang/Float;)V", Float[].class);
	}

	@Test
	public void testBooleanArray() {
		checkMethod("setZz", "([Ljava/lang/Boolean;)V", Boolean[].class);
	}

	@Test
	public void testIntegerArray() {
		checkMethod("setIi", "([Ljava/lang/Integer;)V", Integer[].class);
	}

	@Test
	public void testObject() {
		checkMethod("setO", "(Ljava/lang/Object;)V", Object.class);
	}

	@Test
	public void testString() {
		checkMethod("setX", "(Ljava/lang/String;)V", String.class);
	}

	@Test
	public void testShort() {
		checkMethod("setS", "(Ljava/lang/Short;)V", Short.class);
	}

	@Test
	public void testCharacter() {
		checkMethod("setC", "(Ljava/lang/Character;)V", Character.class);
	}

	@Test
	public void testByte() {
		checkMethod("setB", "(Ljava/lang/Byte;)V", Byte.class);
	}

	@Test
	public void testLong() {
		checkMethod("setL", "(Ljava/lang/Long;)V", Long.class);
	}

	@Test
	public void testDouble() {
		checkMethod("setD", "(Ljava/lang/Double;)V", Double.class);
	}

	@Test
	public void testFloat() {
		checkMethod("setF", "(Ljava/lang/Float;)V", Float.class);
	}

	@Test
	public void testBoolean() {
		checkMethod("setZ", "(Ljava/lang/Boolean;)V", Boolean.class);
	}

	@Test
	public void testInteger() {
		checkMethod("setI", "(Ljava/lang/Integer;)V", Integer.class);
	}

	@Test
	public void testUnboundedGeneric() {
		checkMethod("setGenericObject", "(Ljava/lang/Object;)V", Object.class);
	}

	@Test
	public void testGenericComparable() { // from java.lang
		checkMethod("setGenericComparable", "(Ljava/lang/Comparable;)V", Comparable.class);
	}

	@Test
	public void testGenericSerializable() { // from java.io
		checkMethod("setGenericSerializable", "(Ljava/io/Serializable;)V", Serializable.class);
	}

	@Test
	public void testGenericSelfReference() {
		checkMethod("setNext", "(Lorg/neo4j/ogm/metadata/types/POJO;)V", POJO.class);
	}

	// all methods returning iterables of some type T must resolve to the class of T
	@Test // List<S>
	public void testCollectionWithUnboundGenericParameter() {
		checkMethod("setElements", "TS;", Object.class);
	}

	@Test // List<POJO<S, T, U>> neighbours;
	public void testCollectionWithConcreteParameterizedType() {
		checkMethod("setNeighbours", "Lorg/neo4j/ogm/metadata/types/POJO<TS;TT;TU;", POJO.class);
	}

	@Test // List<? extends Integer> superIntegers
	public void testCollectionWithExtendedConcreteParameterizedType() {
		checkMethod("setSuperIntegers", "+Ljava/lang/Integer;", Object.class);
	}

	@Test    // List<? super Integer> subIntegers;
	public void testCollectionWithReducedConcreteParameterizedType() {
		checkMethod("setSubIntegers", "-Ljava/lang/Integer;", Object.class);
	}

	@Test    // List<? extends S> superS;
	public void testCollectionOfWildcardExtendingGenericType() {
		checkMethod("setSuperS", "+TS;", Object.class);
	}

	@Test    // List<? super S> subS;
	public void testCollectionOfWildcardReducingGenericType() {
		checkMethod("setSubS", "-TS;", Object.class);
	}

	@Test    // List<?>;
	public void testListOfGenericWildcard() {
		checkMethod("setListOfAnything", "*", Object.class);
	}

	@Test    // Vector<?>;
	public void testVectorOfGenericWildcard() {
		checkMethod("setVectorOfAnything", "*", Object.class);
	}

	@Test    // Set<?>;
	public void testSetOfGenericWildcard() {
		checkMethod("setSetOfAnything", "*", Object.class);
	}

	@Test    // Iterable<Map<Class<S>, POJO<S, T, U>>> iterable;
	public void testIterableOfMapOfParameterizedClasses() {
		checkMethod("setIterable", "Ljava/util/Map<Ljava/lang/Class<TS;", Map.class);
	}

}
