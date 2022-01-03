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
package org.neo4j.ogm.domain.satellites;

import org.neo4j.ogm.annotation.Property;

/**
 * This object is partially hydrated via its setter methods and partially via its fields
 * <p/>
 * A Satellite can be logically active or inactive. However this is not determined by
 * an attribute on the node, but instead by the relationship-type to the space program
 * that launched it (program)-[:ACTIVE]->(satellite) or (program-[:INACTIVE]-(satellite).
 * <p/>
 * The requirement to assign boolean attributes from the presence of absence of a relationship
 * between two nodes is a long-standing one. Currently no solution exists. One possibility
 * would be to add a new attribute @Infer. This would only apply to Boolean fields
 * and their related getters/setters, for example:
 *
 * @author Vince Bickers
 * @Infer(relationshipType="ACTIVE") Boolean active;
 * <p/>
 * Does the absence of an 'ACTIVE' relationship imply 'INACTIVE' and therefore active=False?
 */
public class Satellite extends DomainObject {

    @Property("satellite")
    private String name;

    @Property("launch_date")
    private String launched;

    @Property("manned")
    private String manned;

    private Location location;
    private Orbit orbit;

    // incoming relationship
    private Program program;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLaunched() {
        return launched;
    }

    public void setLaunched(String launched) {
        this.launched = launched;
    }

    public String getManned() {
        return manned;
    }

    public void setManned(String manned) {
        this.manned = manned;
    }

    // this relationship is auto-discovered by the OGM because
    // the label on the related object is "Location"
    // therefore, no annotations are required.
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    // this relationship is auto-discovered by the OGM because
    // the label on the related object is "Orbit"
    // therefore, no annotations are required
    public Orbit getOrbit() {
        return orbit;
    }

    public void setOrbit(Orbit orbit) {
        this.orbit = orbit;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }
}
