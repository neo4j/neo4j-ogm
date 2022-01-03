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
package org.neo4j.ogm.persistence.types.nativetypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

@NodeEntity
public class SomethingSpatial {

    private Long id;

    // Convert to native types
    private GeographicPoint2d geographicPoint2d;
    private GeographicPoint3d geographicPoint3d;

    private CartesianPoint2d cartesianPoint2d;
    private CartesianPoint3d cartesianPoint3d;

    // Do not try to convert
    private List<String> stringList = new ArrayList<>();

    @Properties
    private Map<String, Object> properties = new HashMap<>();

    @Relationship("REF")
    private Collection<SomethingRelationship> rels = new ArrayList<>();

    private List<GeographicPoint2d> listOfPoints;

    private GeographicPoint2d[] arrayOfPoints;

    public SomethingSpatial() {
        stringList.add("a");
        stringList.add("b");

        properties.put("a", "a");
        properties.put("b", "b");
    }

    public Long getId() {
        return id;
    }

    public GeographicPoint2d getGeographicPoint2d() {
        return geographicPoint2d;
    }

    public void setGeographicPoint2d(GeographicPoint2d geographicPoint2d) {
        this.geographicPoint2d = geographicPoint2d;
    }

    public GeographicPoint3d getGeographicPoint3d() {
        return geographicPoint3d;
    }

    public void setGeographicPoint3d(GeographicPoint3d geographicPoint3d) {
        this.geographicPoint3d = geographicPoint3d;
    }

    public CartesianPoint2d getCartesianPoint2d() {
        return cartesianPoint2d;
    }

    public void setCartesianPoint2d(CartesianPoint2d cartesianPoint2d) {
        this.cartesianPoint2d = cartesianPoint2d;
    }

    public CartesianPoint3d getCartesianPoint3d() {
        return cartesianPoint3d;
    }

    public void setCartesianPoint3d(CartesianPoint3d cartesianPoint3d) {
        this.cartesianPoint3d = cartesianPoint3d;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Collection<SomethingRelationship> getRels() {
        return rels;
    }

    public void setRels(
        Collection<SomethingRelationship> rels) {
        this.rels = rels;
    }

    public List<GeographicPoint2d> getListOfPoints() {
        return listOfPoints;
    }

    public void setListOfPoints(List<GeographicPoint2d> listOfPoints) {
        this.listOfPoints = listOfPoints;
    }

    public GeographicPoint2d[] getArrayOfPoints() {
        return arrayOfPoints;
    }

    public void setArrayOfPoints(GeographicPoint2d[] arrayOfPoints) {
        this.arrayOfPoints = arrayOfPoints;
    }
}
