package org.neo4j.ogm.mapper.domain.rulers;

public class Empress extends Person implements Ruler {
    @Override
    public String sex() {
        return "Female";
    }

    @Override
    public boolean isCommoner() {
        return false;
    }

    @Override
    public String rulesOver() {
        return "Empire";
    }
}
