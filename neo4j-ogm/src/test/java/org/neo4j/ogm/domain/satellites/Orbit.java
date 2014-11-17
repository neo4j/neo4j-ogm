package org.neo4j.ogm.domain.satellites;

import org.neo4j.ogm.annotation.Property;

/**
 * This object is entirely hydrated via its setters
 */
public class Orbit extends DomainObject {

    private String name;

    @Property(name="orbit")
    public String getName() {
        return name;
    }

    @Property(name="orbit")
    public void setName(String name) {
        this.name = name;
    }
}
