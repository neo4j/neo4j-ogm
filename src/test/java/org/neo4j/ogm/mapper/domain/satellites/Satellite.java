package org.neo4j.ogm.mapper.domain.satellites;

import org.neo4j.ogm.annotation.Property;

/**
 * This object is partially hydrated via its setter methods and partially via its fields
 */
public class Satellite extends DomainObject {

    @Property(name="satellite")
    private String name;

    private String launched;
    private String manned;

    private Location location;
    private Orbit orbit;

    // --> should be on DomainObject
    private Long id;
    private String ref;

    public Long getId() {
        return id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
    // <-- ends

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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Orbit getOrbit() {
        return orbit;
    }

    public void setOrbit(Orbit orbit) {
        this.orbit = orbit;
    }

}
