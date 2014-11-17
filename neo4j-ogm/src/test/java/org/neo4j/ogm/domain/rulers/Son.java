package org.neo4j.ogm.domain.rulers;

public class Son extends Person {
    @Override
    public String sex() {
        return "Male";
    }

    @Override
    public boolean isCommoner() {
        return true;
    }
}
