package org.neo4j.ogm.domain.autoindex.invalid;

import org.neo4j.ogm.annotation.CompositeIndex;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Frantisek Hartman
 */
@NodeEntity(label = "Entity")
@CompositeIndex({ "name", "ag" })
public class WrongPropertyCompositeIndexEntity {

    Long id;

    String name;

    int age;
}
