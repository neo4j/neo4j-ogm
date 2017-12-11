package org.neo4j.ogm.domain.locking;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Version;

/**
 * @author Frantisek Hartman
 */
@NodeEntity
public class Location {

    Long id;

    @Version
    Long customVersion;

    String name;

    public Location() {
    }

    public Location(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomVersion() {
        return customVersion;
    }

    public void setCustomVersion(Long customVersion) {
        this.customVersion = customVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
