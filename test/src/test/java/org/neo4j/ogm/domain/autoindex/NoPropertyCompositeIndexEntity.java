package org.neo4j.ogm.domain.autoindex;

import org.neo4j.ogm.annotation.CompositeIndex;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Frantisek Hartman
 */
@NodeEntity(label = "Entity")
@CompositeIndex
public class NoPropertyCompositeIndexEntity {

    Long id;

    String name;

    int age;
}
