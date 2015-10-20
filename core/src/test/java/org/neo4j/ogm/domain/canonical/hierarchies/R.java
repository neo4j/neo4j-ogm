package org.neo4j.ogm.domain.canonical.hierarchies;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author vince
 */
public abstract class R {
    private Long id;

    @StartNode
    private A a;

    @EndNode
    private B b;

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

}
