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
package org.neo4j.ogm.domain.canonical;

import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * This class defines every type we should be able to map to.
 *
 * @author Vince Bickers
 */
public class Mappable {

    private Long id;

    private int primitiveInt;
    private long primitiveLong;
    private byte primitiveByte;
    private short primitiveShort;
    private boolean primitiveBoolean;
    private char primitiveChar;
    private float primitiveFloat;
    private double primitiveDouble;

    private int[] primitiveIntArray;
    private long[] primitiveLongArray;
    private byte[] primitiveByteArray;
    private short[] primitiveShortArray;
    private boolean[] primitiveBooleanArray;
    private char[] primitiveCharArray;
    private float[] primitiveFloatArray;
    private double[] primitiveDoubleArray;

    private Integer objectInteger;
    private Long objectLong;
    private Byte objectByte;
    private Short objectShort;
    private Boolean objectBoolean;
    private Float objectFloat;
    private Double objectDouble;
    private String objectString;
    private Character objectCharacter;

    private Integer[] objectIntegerArray;
    private Long[] objectLongArray;
    private Byte[] objectByteArray;
    private Short[] objectShortArray;
    private Boolean[] objectBooleanArray;
    private Float[] objectFloatArray;
    private Double[] objectDoubleArray;
    private String[] objectStringArray;
    private Character[] objectCharArray;

    private List<?> listOfAnything;
    private Set<?> setOfAnything;
    private Vector<?> vectorOfAnything;
    private List<String> listOfString;
    private List<Character> listOfCharacter;

    public int getPrimitiveInt() {
        return primitiveInt;
    }

    public void setPrimitiveInt(int primitiveInt) {
        this.primitiveInt = primitiveInt;
    }

    public long getPrimitiveLong() {
        return primitiveLong;
    }

    public void setPrimitiveLong(long primitiveLong) {
        this.primitiveLong = primitiveLong;
    }

    public byte getPrimitiveByte() {
        return primitiveByte;
    }

    public void setPrimitiveByte(byte primitiveByte) {
        this.primitiveByte = primitiveByte;
    }

    public short getPrimitiveShort() {
        return primitiveShort;
    }

    public void setPrimitiveShort(short primitiveShort) {
        this.primitiveShort = primitiveShort;
    }

    public boolean isPrimitiveBoolean() {
        return primitiveBoolean;
    }

    public void setPrimitiveBoolean(boolean primitiveBoolean) {
        this.primitiveBoolean = primitiveBoolean;
    }

    public char getPrimitiveChar() {
        return primitiveChar;
    }

    public void setPrimitiveChar(char primitiveChar) {
        this.primitiveChar = primitiveChar;
    }

    public float getPrimitiveFloat() {
        return primitiveFloat;
    }

    public void setPrimitiveFloat(float primitiveFloat) {
        this.primitiveFloat = primitiveFloat;
    }

    public double getPrimitiveDouble() {
        return primitiveDouble;
    }

    public void setPrimitiveDouble(double primitiveDouble) {
        this.primitiveDouble = primitiveDouble;
    }

    public int[] getPrimitiveIntArray() {
        return primitiveIntArray;
    }

    public void setPrimitiveIntArray(int[] primitiveIntArray) {
        this.primitiveIntArray = primitiveIntArray;
    }

    public long[] getPrimitiveLongArray() {
        return primitiveLongArray;
    }

    public void setPrimitiveLongArray(long[] primitiveLongArray) {
        this.primitiveLongArray = primitiveLongArray;
    }

    public byte[] getPrimitiveByteArray() {
        return primitiveByteArray;
    }

    public void setPrimitiveByteArray(byte[] primitiveByteArray) {
        this.primitiveByteArray = primitiveByteArray;
    }

    public short[] getPrimitiveShortArray() {
        return primitiveShortArray;
    }

    public void setPrimitiveShortArray(short[] primitiveShortArray) {
        this.primitiveShortArray = primitiveShortArray;
    }

    public boolean[] getPrimitiveBooleanArray() {
        return primitiveBooleanArray;
    }

    public void setPrimitiveBooleanArray(boolean[] primitiveBooleanArray) {
        this.primitiveBooleanArray = primitiveBooleanArray;
    }

    public char[] getPrimitiveCharArray() {
        return primitiveCharArray;
    }

    public void setPrimitiveCharArray(char[] primitiveCharArray) {
        this.primitiveCharArray = primitiveCharArray;
    }

    public float[] getPrimitiveFloatArray() {
        return primitiveFloatArray;
    }

    public void setPrimitiveFloatArray(float[] primitiveFloatArray) {
        this.primitiveFloatArray = primitiveFloatArray;
    }

    public double[] getPrimitiveDoubleArray() {
        return primitiveDoubleArray;
    }

    public void setPrimitiveDoubleArray(double[] primitiveDoubleArray) {
        this.primitiveDoubleArray = primitiveDoubleArray;
    }

    public Integer getObjectInteger() {
        return objectInteger;
    }

    public void setObjectInteger(Integer objectInteger) {
        this.objectInteger = objectInteger;
    }

    public Long getObjectLong() {
        return objectLong;
    }

    public void setObjectLong(Long objectLong) {
        this.objectLong = objectLong;
    }

    public Byte getObjectByte() {
        return objectByte;
    }

    public void setObjectByte(Byte objectByte) {
        this.objectByte = objectByte;
    }

    public Short getObjectShort() {
        return objectShort;
    }

    public void setObjectShort(Short objectShort) {
        this.objectShort = objectShort;
    }

    public Boolean getObjectBoolean() {
        return objectBoolean;
    }

    public void setObjectBoolean(Boolean objectBoolean) {
        this.objectBoolean = objectBoolean;
    }

    public Float getObjectFloat() {
        return objectFloat;
    }

    public void setObjectFloat(Float objectFloat) {
        this.objectFloat = objectFloat;
    }

    public Double getObjectDouble() {
        return objectDouble;
    }

    public void setObjectDouble(Double objectDouble) {
        this.objectDouble = objectDouble;
    }

    public Integer[] getObjectIntegerArray() {
        return objectIntegerArray;
    }

    public void setObjectIntegerArray(Integer[] objectIntegerArray) {
        this.objectIntegerArray = objectIntegerArray;
    }

    public Long[] getObjectLongArray() {
        return objectLongArray;
    }

    public void setObjectLongArray(Long[] objectLongArray) {
        this.objectLongArray = objectLongArray;
    }

    public Byte[] getObjectByteArray() {
        return objectByteArray;
    }

    public void setObjectByteArray(Byte[] objectByteArray) {
        this.objectByteArray = objectByteArray;
    }

    public Short[] getObjectShortArray() {
        return objectShortArray;
    }

    public void setObjectShortArray(Short[] objectShortArray) {
        this.objectShortArray = objectShortArray;
    }

    public Boolean[] getObjectBooleanArray() {
        return objectBooleanArray;
    }

    public void setObjectBooleanArray(Boolean[] objectBooleanArray) {
        this.objectBooleanArray = objectBooleanArray;
    }

    public Float[] getObjectFloatArray() {
        return objectFloatArray;
    }

    public void setObjectFloatArray(Float[] objectFloatArray) {
        this.objectFloatArray = objectFloatArray;
    }

    public Double[] getObjectDoubleArray() {
        return objectDoubleArray;
    }

    public void setObjectDoubleArray(Double[] objectDoubleArray) {
        this.objectDoubleArray = objectDoubleArray;
    }

    public List<?> getListOfAnything() {
        return listOfAnything;
    }

    public void setListOfAnything(List<?> listOfAnything) {
        this.listOfAnything = listOfAnything;
    }

    public Set<?> getSetOfAnything() {
        return setOfAnything;
    }

    public void setSetOfAnything(Set<?> setOfAnything) {
        this.setOfAnything = setOfAnything;
    }

    public Vector<?> getVectorOfAnything() {
        return vectorOfAnything;
    }

    public void setVectorOfAnything(Vector<?> vectorOfAnything) {
        this.vectorOfAnything = vectorOfAnything;
    }

    public String getObjectString() {
        return objectString;
    }

    public void setObjectString(String objectString) {
        this.objectString = objectString;
    }

    public String[] getObjectStringArray() {
        return objectStringArray;
    }

    public void setObjectStringArray(String[] objectStringArray) {
        this.objectStringArray = objectStringArray;
    }

    public Character getObjectCharacter() {
        return objectCharacter;
    }

    public void setObjectCharacter(Character objectCharacter) {
        this.objectCharacter = objectCharacter;
    }

    public Character[] getObjectCharArray() {
        return objectCharArray;
    }

    public void setObjectCharArray(Character[] objectCharArray) {
        this.objectCharArray = objectCharArray;
    }

    public List<String> getListOfString() {
        return listOfString;
    }

    public void setListOfString(List<String> listOfString) {
        this.listOfString = listOfString;
    }

    public List<Character> getListOfCharacter() {
        return listOfCharacter;
    }

    public void setListOfCharacter(List<Character> listOfCharacter) {
        this.listOfCharacter = listOfCharacter;
    }
}
