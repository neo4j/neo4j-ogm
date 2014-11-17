package org.neo4j.ogm.domain.satellites;

import org.neo4j.ogm.annotation.Property;

/**
 * This object is partially hydrated via its setter methods and partially via its fields
 *
 * A Satellite can be logically active or inactive. However this is not determined by
 * an attribute on the node, but instead by the relationship-type to the space program
 * that launched it (program)-[:ACTIVE]->(satellite) or (program-[:INACTIVE]-(satellite).
 *
 * The requirement to assign boolean attributes from the presence of absence of a relationship
 * between two nodes is a long-standing one. Currently no solution exists. One possibility
 * would be to add a new attribute @Infer. This would only apply to Boolean fields
 * and their related getters/setters, for example:
 *
 * @Infer(relationshipType="ACTIVE") Boolean active;
 *
 * Does the absence of an 'ACTIVE' relationship imply 'INACTIVE' and therefore active=False?
 *
 */
public class Satellite extends DomainObject {

    @Property(name="satellite")
    private String name;

    private String launched;
    private String manned;

    private Location location;
    private Orbit orbit;

    public String getName() {
        return name;
    }

    @Property(name="satellite")
    public void setName(String name) {
        this.name = name;
    }

    @Property(name="launch_date")
    public String getLaunched() {
        return launched;
    }

    @Property(name="launch_date")
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

    // incoming relationship
    private Program program;

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }
}
