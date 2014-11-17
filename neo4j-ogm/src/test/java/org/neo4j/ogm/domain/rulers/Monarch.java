package org.neo4j.ogm.domain.rulers;

public abstract class Monarch extends Person implements Ruler {

    public String rulesOver() {
        return "Kingdom";
    }

    @Override
    public boolean isCommoner() {
        return false;
    }

}
