/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.domain.canonical;

import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * This class defines every type we should be able to map to.
 */
public class Mappable {

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

    private Integer[] objectIntegerArray;
    private Long[] objectLongArray;
    private Byte[] objectByteArray;
    private Short[] objectShortArray;
    private Boolean[] objectBooleanArray;
    private Float[] objectFloatArray;
    private Double[] objectDoubleArray;
    private String[] objectStringArray;

    private List<?> listOfAnything;
    private Set<?> setOfAnything;
    private Vector<?> vectorOfAnything;

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
}
