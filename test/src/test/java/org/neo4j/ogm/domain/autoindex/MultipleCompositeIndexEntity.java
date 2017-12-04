package org.neo4j.ogm.domain.autoindex;

import org.neo4j.ogm.annotation.CompositeIndex;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Frantisek Hartman
 */
@NodeEntity(label = "Entity")
@CompositeIndex({ "name", "age" })
@CompositeIndex({ "name", "email" })
public class MultipleCompositeIndexEntity {

    Long id;

    String name;

    int age;

    String email;
}
