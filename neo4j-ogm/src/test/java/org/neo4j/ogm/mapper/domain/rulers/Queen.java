package org.neo4j.ogm.mapper.domain.rulers;

public class Queen extends Monarch {

    @Override
    public String sex() {
        return "Female";
    }
}
