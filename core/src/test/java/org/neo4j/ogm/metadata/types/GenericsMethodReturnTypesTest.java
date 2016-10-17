/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "()License").
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
public class GenericsMethodReturnTypesTest extends TestMetaDataTypeResolution {

	@Test    // <S> Iterable<S> map(Class<S> key, Node<S, T, U> value)
	public void testIterableOfMapOfParameterizedClassesMethod() {
		checkMethod("getMap", "X:Ljava/lang/Long;", Long.class);
	}

	@Test
	public void testPrimitiveShortArray() {
		checkMethod("getPss", "()[S", short.class);
	}

	@Test
	public void testPrimitiveCharArray() {
		checkMethod("getPcc", "()[C", char.class);
	}

	@Test
	public void testPrimitiveByteArray() {
		checkMethod("getPbb", "()[B", byte.class);
	}

	@Test
	public void testPrimitiveLongArray() {
		checkMethod("getPll", "()[J", long.class);
	}

	@Test
	public void testPrimitiveDoubleArray() {
		checkMethod("getPdd", "()[D", double.class);
	}

	@Test
	public void testPrimitiveFloatArray() {
		checkMethod("getPff", "()[F", float.class);
	}

	@Test
	public void testPrimitiveBooleanArray() {
		checkMethod("getPzz", "()[Z", boolean.class);
	}

	@Test
	public void testPrimitiveIntegerArray() {
		checkMethod("getPii", "()[I", int.class);
	}

	@Test
	public void testPrimitiveShort() {
		checkMethod("getPs", "()S", short.class);
	}

	@Test
	public void testPrimitiveChar() {
		checkMethod("getPc", "()C", char.class);
	}

	@Test
	public void testPrimitiveByte() {
		checkMethod("getPb", "()B", byte.class);
	}

	@Test
	public void testPrimitiveLong() {
		checkMethod("getPl", "()J", long.class);
	}

	@Test
	public void testPrimitiveDouble() {
		checkMethod("getPd", "()D", double.class);
	}

	@Test
	public void testPrimitiveFloat() {
		checkMethod("getPf", "()F", float.class);
	}

	@Test
	public void testPrimitiveBoolean() {
		checkMethod("getPz", "()Z", boolean.class);
	}

	@Test
	public void testPrimitiveInteger() {
		checkMethod("getPi", "()I", int.class);
	}

	@Test
	public void testObjectArray () {
		checkMethod("getOo", "()[Ljava/lang/Object;", Object.class);
	}

	@Test
	public void testStringArray () {
		checkMethod("getXx", "()[Ljava/lang/String;", String.class);
	}

	@Test
	public void testShortArray() {
		checkMethod("getSs", "()[Ljava/lang/Short;", Short.class);
	}

	@Test
	public void testCharacterArray() {
		checkMethod("getCc", "()[Ljava/lang/Character;", Character.class);
	}                

	@Test
	public void testByteArray() {
		checkMethod("getBb", "()[Ljava/lang/Byte;", Byte.class);
	}

	@Test
	public void testLongArray() {
		checkMethod("getLl", "()[Ljava/lang/Long;", Long.class);
	}

	@Test
	public void testDoubleArray() {
		checkMethod("getDd", "()[Ljava/lang/Double;", Double.class);
	}

	@Test
	public void testFloatArray() {
		checkMethod("getFf", "()[Ljava/lang/Float;", Float.class);
	}

	@Test
	public void testBooleanArray() {
		checkMethod("getZz", "()[Ljava/lang/Boolean;", Boolean.class);
	}

	@Test
	public void testIntegerArray() {
		checkMethod("getIi", "()[Ljava/lang/Integer;", Integer.class);
	}

	@Test
	public void testObject() {
		checkMethod("getO", "()Ljava/lang/Object;", Object.class);
	}

	@Test
	public void testString() {
		checkMethod("getX", "()Ljava/lang/String;", String.class);
	}

	@Test
	public void testShort() {
		checkMethod("getS", "()Ljava/lang/Short;", Short.class);
	}

	@Test
	public void testCharacter() {
		checkMethod("getC", "()Ljava/lang/Character;", Character.class);
	}

	@Test
	public void testByte() {
		checkMethod("getB", "()Ljava/lang/Byte;", Byte.class);
	}

	@Test
	public void testLong() {
		checkMethod("getL", "()Ljava/lang/Long;", Long.class);
	}

	@Test
	public void testDouble() {
		checkMethod("getD", "()Ljava/lang/Double;", Double.class);
	}

	@Test
	public void testFloat() {
		checkMethod("getF", "()Ljava/lang/Float;", Float.class);
	}

	@Test
	public void testBoolean() {
		checkMethod("getZ", "()Ljava/lang/Boolean;", Boolean.class);
	}

	@Test
	public void testInteger() {
		checkMethod("getI", "()Ljava/lang/Integer;", Integer.class);
	}

	@Test
	public void testUnboundedGeneric() {
		checkMethod("getGenericObject", "()Ljava/lang/Object;", Object.class);
	}

	@Test
	public void testGenericComparable() { // from java.lang
		checkMethod("getGenericComparable", "()Ljava/lang/Comparable;", Comparable.class);
	}

	@Test
	public void testGenericSerializable() { // from java.io
		checkMethod("getGenericSerializable", "()Ljava/io/Serializable;", Serializable.class);
	}

	@Test
	public void testGenericSelfReference() {
		checkMethod("getNext", "()Lorg/neo4j/ogm/metadata/types/POJO;", POJO.class);
	}

	// all methods returning iterables of some type T must resolve to the class of T
	@Test // List<S>
	public void testCollectionWithUnboundGenericParameter() {
		checkMethod("getElements", "TS;", Object.class);
	}

	@Test // List<POJO<S, T, U>> neighbours;
	public void testCollectionWithConcreteParameterizedType() {
		checkMethod("getNeighbours", "Lorg/neo4j/ogm/metadata/types/POJO<TS;TT;TU;", POJO.class);
	}

	@Test // List<? extends Integer> superIntegers
	public void testCollectionWithExtendedConcreteParameterizedType() {
		checkMethod("getSuperIntegers", "+Ljava/lang/Integer;", Object.class);
	}

	@Test    // List<? super Integer> subIntegers;
	public void testCollectionWithReducedConcreteParameterizedType() {
		checkMethod("getSubIntegers", "-Ljava/lang/Integer;", Object.class);
	}

	@Test    // List<? extends S> superS;
	public void testCollectionOfWildcardExtendingGenericType() {
		checkMethod("getSuperS", "+TS;", Object.class);
	}

	@Test    // List<? super S> subS;
	public void testCollectionOfWildcardReducingGenericType() {
		checkMethod("getSubS", "-TS;", Object.class);
	}

	@Test    // List<?>;
	public void testListOfGenericWildcard() {
		checkMethod("getListOfAnything", "*", Object.class);
	}

	@Test    // Vector<?>;
	public void testVectorOfGenericWildcard() {
		checkMethod("getVectorOfAnything", "*", Object.class);
	}

	@Test    // Set<?>;
	public void testSetOfGenericWildcard() {
		checkMethod("getSetOfAnything", "*", Object.class);
	}

	@Test    // Iterable<Map<Class<S>, POJO<S, T, U>>> iterable;
	public void testIterableOfMapOfParameterizedClasses() {
		checkMethod("getIterable", "Ljava/util/Map<Ljava/lang/Class<TS;", Map.class);
	}

}
