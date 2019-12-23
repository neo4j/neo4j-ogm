package org.neo4j.ogm.domain.gh726.package_a;

import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Gerrit Meier
 */
@NodeEntity("SameClassA")
public class SameClass {

    private Long id;

    private String name;

    public Long getId() {
        return id;
    }
}
