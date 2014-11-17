package org.neo4j.ogm.domain.rulers;

public class Queen extends Monarch {

    @Override
    public String sex() {
        return "Female";
    }
}
