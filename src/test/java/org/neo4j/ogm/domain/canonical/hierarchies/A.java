package org.neo4j.ogm.domain.canonical.hierarchies;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author vince
 */
@NodeEntity
public class A {
    private Long id;


    @Relationship
    private R r;

    public R getR() {
        return r;
    }


    public void setR(R c) {
        this.r = c;
    }
}
