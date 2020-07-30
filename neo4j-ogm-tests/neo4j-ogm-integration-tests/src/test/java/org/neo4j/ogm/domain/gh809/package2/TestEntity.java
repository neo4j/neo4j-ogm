package org.neo4j.ogm.domain.gh809.package2;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Gerrit Meier
 */
@NodeEntity("TestEntity2")
public class TestEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}
