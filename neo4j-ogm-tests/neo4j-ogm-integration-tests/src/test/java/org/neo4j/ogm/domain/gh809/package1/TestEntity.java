package org.neo4j.ogm.domain.gh809.package1;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

/**
 * @author Gerrit Meier
 */
public class TestEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}
