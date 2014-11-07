package org.neo4j.ogm.mapper.domain.satellites;

import org.neo4j.ogm.annotation.Property;

/**
 * This object is entirely hydrated via its setters
 */
public class Orbit extends DomainObject {

    private String name;
//    private Long id;
//    private String ref;
//
//    public Long getId() {
//        return id;
//    }
//
//    public String getRef() {
//        return ref;
//    }
//
//    public void setRef(String ref) {
//        this.ref = ref;
//    }

    @Property(name="orbit")
    public String getName() {
        return name;
    }

    @Property(name="orbit")
    public void setName(String name) {
        this.name = name;
    }
}
