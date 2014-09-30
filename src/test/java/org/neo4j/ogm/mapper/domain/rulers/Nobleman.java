package org.neo4j.ogm.mapper.domain.rulers;

public class Nobleman extends Person implements Ruler {

    @Override
    public String sex() {
        return "Male";
    }

    @Override
    public boolean isCommoner() {
        return false;
    }

    @Override
    public String rulesOver() {
        return null;
    }
}
