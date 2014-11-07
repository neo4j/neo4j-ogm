package org.neo4j.ogm.mapper.domain.satellites;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity(label="Space_Program")
public class Program extends DomainObject {

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

    @Property(name="program")
    public String getName() {
        return name;
    }

    @Property(name="program")
    public void setName(String name) {
        this.name = name;
    }

}
