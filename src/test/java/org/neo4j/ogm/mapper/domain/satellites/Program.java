package org.neo4j.ogm.mapper.domain.satellites;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

import java.util.List;

@NodeEntity(label="Space_Program")
public class Program extends DomainObject {

    private String name;
    private List<Satellite> satellites;

    @Property(name="program")
    public String getName() {
        return name;
    }

    @Property(name="program")
    public void setName(String name) {
        this.name = name;
    }

    public List<Satellite> getSatellites() {
        return satellites;
    }

    public void setSatellites(List<Satellite> satellites) {
        this.satellites = satellites;
    }
}
