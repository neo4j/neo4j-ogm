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

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;
import org.neo4j.ogm.types.spatial.CartesianPoint3d;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;
import org.neo4j.ogm.types.spatial.GeographicPoint3d;

@RelationshipEntity("REF")
public class SomethingRelationship {

    private Long id;

    // Convert to native types
    private GeographicPoint2d geographicPoint2d;
    private GeographicPoint3d geographicPoint3d;

    private CartesianPoint2d cartesianPoint2d;
    private CartesianPoint3d cartesianPoint3d;

    @StartNode
    private SomethingSpatial sometimeStart = new SomethingSpatial();

    @EndNode
    private SomethingRelated someEnd = new SomethingRelated();

    public SomethingRelationship() {
        sometimeStart.getRels().add(this);
        someEnd.getRels().add(this);
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
}
