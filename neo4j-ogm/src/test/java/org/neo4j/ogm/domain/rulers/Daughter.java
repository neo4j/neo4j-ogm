package org.neo4j.ogm.domain.rulers;

public class Daughter extends Person {
    @Override
    public String sex() {
        return "Female";
    }

    @Override
    public boolean isCommoner() {
        return true;
    }


}
