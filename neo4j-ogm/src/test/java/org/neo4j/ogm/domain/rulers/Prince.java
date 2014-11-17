package org.neo4j.ogm.domain.rulers;

public class Prince extends Son implements Ruler {

    @Override
    public boolean isCommoner() {
        return false;
    }

    @Override
    public String rulesOver() {
        return "Principality";
    }
}
