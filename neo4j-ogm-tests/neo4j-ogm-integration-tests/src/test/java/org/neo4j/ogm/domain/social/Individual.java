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
package org.neo4j.ogm.domain.social;

import java.util.List;
import java.util.Vector;

/**
 * Arbitrary POJO used to test mapping code.
 *
 * @author Adam George
 * @author Luanne Misquitta
 */
public class Individual {

    public float[] primitiveFloatArray;
    public Integer[] integerArray;
    public Float[] floatArray;
    public List<Integer> integerCollection;
    private Long id;
    private String name;
    private int age;
    private float bankBalance;
    private byte code;
    private Integer numberOfPets;
    private Float distanceFromZoo;
    private Byte numberOfShoes;
    private Double maxTemp;
    private List<Individual> friends;
    private int[] primitiveIntArray;
    private byte[] primitiveByteArray;
    private List<Float> floatCollection;
    private List<Byte> byteCollection;
    private List<Long> longCollection;

    private Vector<Double> favouriteRadioStations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(float bankBalance) {
        this.bankBalance = bankBalance;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public Integer getNumberOfPets() {
        return numberOfPets;
    }

    public void setNumberOfPets(Integer numberOfPets) {
        this.numberOfPets = numberOfPets;
    }

    public Float getDistanceFromZoo() {
        return distanceFromZoo;
    }

    public void setDistanceFromZoo(Float distanceFromZoo) {
        this.distanceFromZoo = distanceFromZoo;
    }

    public Byte getNumberOfShoes() {
        return numberOfShoes;
    }

    public void setNumberOfShoes(Byte numberOfShoes) {
        this.numberOfShoes = numberOfShoes;
    }

    public List<Individual> getFriends() {
        return friends;
    }

    public void setFriends(List<Individual> friends) {
        this.friends = friends;
    }

    public int[] getPrimitiveIntArray() {
        return primitiveIntArray;
    }

    public void setPrimitiveIntArray(int[] primitiveIntArray) {
        this.primitiveIntArray = primitiveIntArray;
    }

    public Vector<Double> getFavouriteRadioStations() {
        return favouriteRadioStations;
    }

    public void setFavouriteRadioStations(Vector<Double> fmFrequencies) {
        this.favouriteRadioStations = fmFrequencies;
    }

    public byte[] getPrimitiveByteArray() {
        return primitiveByteArray;
    }

    public void setPrimitiveByteArray(byte[] primitiveByteArray) {
        this.primitiveByteArray = primitiveByteArray;
    }

    public List<Float> getFloatCollection() {
        return floatCollection;
    }

    public void setFloatCollection(List<Float> floatCollection) {
        this.floatCollection = floatCollection;
    }

    public List<Byte> getByteCollection() {
        return byteCollection;
    }

    public void setByteCollection(List<Byte> byteCollection) {
        this.byteCollection = byteCollection;
    }

    public Double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(Double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public List<Long> getLongCollection() {
        return longCollection;
    }

    public void setLongCollection(List<Long> longCollection) {
        this.longCollection = longCollection;
    }
}
